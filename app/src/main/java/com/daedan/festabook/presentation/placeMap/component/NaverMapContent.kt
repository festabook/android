package com.daedan.festabook.presentation.placeMap.component

import android.content.ComponentCallbacks2
import android.content.res.Configuration
import android.os.Bundle
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.daedan.festabook.presentation.placeMap.intent.state.MapDelegate
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import kotlinx.coroutines.suspendCancellableCoroutine

@Composable
fun NaverMapContent(
    modifier: Modifier = Modifier,
    mapDelegate: MapDelegate = MapDelegate(),
    onMapDrag: () -> Unit = {},
    onMapReady: (NaverMap) -> Unit = {},
    content: @Composable (NaverMap?) -> Unit,
) {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }
    LaunchedEffect(mapView) {
        val naverMap = mapView.getMapAndRunCallback(onMapReady)
        mapDelegate.initMap(naverMap)
    }
    Box(modifier = modifier) {
        // TODO AndroidView와 CMP 뷰의 혼용으로 컴파일러 경고 발생중 -> 추후 해결하겠습니다
        AndroidView(
            factory = { mapView },
            modifier = Modifier.dragInterceptor(onMapDrag),
            onRelease = {
                mapView.onDestroy()
            },
        )
        content(mapDelegate.value)
    }
    RegisterMapLifeCycle(mapView)
}

private fun Modifier.dragInterceptor(onMapDrag: () -> Unit): Modifier =
    this.then(
        Modifier.pointerInput(Unit) {
            val touchSlop = viewConfiguration.touchSlop // 시스템이 정의한 드래그 판단 기준 거리
            awaitPointerEventScope {
                while (true) {
                    // 1. 첫 번째 터치(Down)를 기다립니다.
                    val downEvent = awaitPointerEvent(pass = PointerEventPass.Initial)
                    val downChange = downEvent.changes.firstOrNull { it.pressed } ?: continue

                    // 터치 시작 지점 저장
                    val startPosition = downChange.position
                    var isDragEmitted = false // 이번 드래그 세션에서 콜백을 호출했는지 체크

                    // 2. 터치가 유지되는 동안(드래그 중) 계속 감시합니다.
                    do {
                        val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                        val change = event.changes.firstOrNull { it.id == downChange.id }

                        if (change != null && change.pressed) {
                            // 현재 위치와 시작 위치 사이의 거리 계산
                            val currentPosition = change.position
                            val distance = (currentPosition - startPosition).getDistance()

                            // 3. 이동 거리가 touchSlop보다 크고, 아직 콜백을 안 불렀다면 호출
                            if (!isDragEmitted && distance > touchSlop) {
                                onMapDrag()
                                isDragEmitted = true
                            }
                        }
                    } while (event.changes.any { it.pressed }) // 손을 뗄 때까지 루프
                }
            }
        },
    )

@Composable
private fun RegisterMapLifeCycle(mapView: MapView) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val previousState = remember { mutableStateOf(Lifecycle.Event.ON_CREATE) }
    val savedInstanceState = rememberSaveable { Bundle() }

    DisposableEffect(lifecycle, mapView) {
        val mapLifecycleObserver =
            mapView.lifecycleObserver(
                savedInstanceState.takeUnless { it.isEmpty },
                previousState,
            )

        val callbacks =
            object : ComponentCallbacks2 {
                override fun onConfigurationChanged(config: Configuration) = Unit

                @Deprecated("This callback is superseded by onTrimMemory")
                override fun onLowMemory() {
                    mapView.onLowMemory()
                }

                override fun onTrimMemory(level: Int) {
                    mapView.onLowMemory()
                }
            }

        lifecycle.addObserver(mapLifecycleObserver)
        context.registerComponentCallbacks(callbacks)
        onDispose {
            mapView.onSaveInstanceState(savedInstanceState)
            lifecycle.removeObserver(mapLifecycleObserver)
            context.unregisterComponentCallbacks(callbacks)
        }
    }
}

private fun MapView.lifecycleObserver(
    savedInstanceState: Bundle?,
    previousState: MutableState<Lifecycle.Event>,
): LifecycleEventObserver =
    LifecycleEventObserver { _, event ->
        when (event) {
            Lifecycle.Event.ON_CREATE -> this.onCreate(savedInstanceState)
            Lifecycle.Event.ON_START -> this.onStart()
            Lifecycle.Event.ON_RESUME -> this.onResume()
            Lifecycle.Event.ON_PAUSE -> this.onPause()
            Lifecycle.Event.ON_STOP -> this.onStop()
            Lifecycle.Event.ON_DESTROY -> this.onDestroy()
            else -> throw IllegalStateException()
        }
        previousState.value = event
    }

private suspend fun MapView.getMapAndRunCallback(onMapReady: (NaverMap) -> Unit = {}): NaverMap =
    suspendCancellableCoroutine { continuation ->
        getMapAsync { map ->
            onMapReady(map)
            continuation.resumeWith(
                Result.success(map),
            )
        }
    }
