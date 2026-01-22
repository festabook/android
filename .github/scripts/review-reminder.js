module.exports = async ({github, context, core}) => {

    // 최소 PR 생성 시간
    const LIMIT_HOURS = 12;
    const LIMIT_MS = LIMIT_HOURS * 60 * 60 * 1000;

    const WEBHOOK_URL = process.env.SLACK_WEBHOOK_REVIEW;

    // GitHub, Slack 정보
    const USER_MAP = {
        'etama123': 'U0995MPSZ62',
        'oungsi2000': 'U098U2R57NK',
        'parkjiminnnn': 'U098U8SLXHD'
    };

    const repoName = context.repo.repo;

    if (!WEBHOOK_URL) {
        core.setFailed("❌ Error: SLACK_WEBHOOK_REVIEW 환경변수가 설정되지 않았습니다.");
        return;
    }

    try {
        // GitHub API 호출로 Open PR 목록 요청
        const {data: prs} = await github.rest.pulls.list({
            owner: context.repo.owner,
            repo: context.repo.repo,
            state: 'open',             // 오픈 PR
            sort: 'created',           // 정렬 기준
            direction: 'asc'          // 오름차순
        });

        const now = new Date();
        const delayedPrs = [];  // 알림 대상

        for (const pr of prs) {
            const createdDate = new Date(pr.created_at);
            const diffTime = now - createdDate; // PR 생성 시간 차이 (ms)

            if (diffTime >= LIMIT_MS) {

                // 리뷰어 정보 가공 (없으면 '미지정' 처리)
                const reviewers = pr.requested_reviewers.length > 0
                    ? pr.requested_reviewers.map(r => {
                        const slackId = USER_MAP[r.login];
                        return slackId ? `<@${slackId}>` : r.login;
                    }).join(', ')
                    : '(리뷰어 미지정)';

                // 지난 시간
                const passedHours = Math.floor(diffTime / (1000 * 60 * 60));

                const authorSlackId = USER_MAP[pr.user.login];
                const authorDisplay = authorSlackId ? `<@${authorSlackId}>` : pr.user.login;

                // 알림 전송 객체 생성
                delayedPrs.push({
                    title: pr.title,
                    url: pr.html_url,
                    author: authorDisplay,
                    reviewers: reviewers,
                    hours: passedHours
                });
            }
        }

        // 지연 PR 없을 시 종료
        if (delayedPrs.length === 0) {
            return;
        }

        const message = {
            text: `🚨 (${repoName}) 코드 리뷰 리마인더`,
            blocks: [
                {
                    "type": "header",
                    "text": {
                        "type": "plain_text",
                        "text": `🔥 (${repoName}) 코드 리뷰 리마인더`,
                        "emoji": true
                    }
                },
                {
                    "type": "section",
                    "text": {
                        "type": "mrkdwn",
                        "text": `현재 *${LIMIT_HOURS}시간* 이상 대기 중인 PR이 *${delayedPrs.length}건* 있습니다.`
                    }
                },
                {"type": "divider"}
            ]
        };

        // 지연 PR 하나씩 메시지 추가
        delayedPrs.forEach((pr, index) => {
            message.blocks.push({
                "type": "section",
                "text": {
                    "type": "mrkdwn",
                    "text": `*${index + 1}. <${pr.url}|${pr.title}>*\n` +
                        `⏳ *${pr.hours}시간* 경과\n` +
                        `👤 작성자: ${pr.author}\n` +
                        `👀 리뷰어: ${pr.reviewers}`
                }
            });
            message.blocks.push({"type": "divider"});
        });

        const response = await fetch(WEBHOOK_URL, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(message)
        });

        if (!response.ok) {
            throw new Error(`Slack 전송 실패 Status: ${response.status}`);
        }

    } catch (error) {
        core.setFailed(`❌ 스크립트 실행 중 에러 발생: ${error.message}`);
    }
};
