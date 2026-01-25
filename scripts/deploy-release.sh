#!/bin/bash

###############################################################################
# deploy-release.sh - Maven Central (Sonatype OSSRH)에 Release 배포
#
# 사용법: MAVEN_USERNAME=user MAVEN_PASSWORD=pass ./scripts/deploy-release.sh
#         또는 ~/.gradle/gradle.properties에 자격 증명 설정
#
# 사전 준비:
#   1. Sonatype OSSRH 계정 생성: https://central.sonatype.org/publish/
#   2. GPG 키 생성 및 등록
#   3. ~/.gradle/gradle.properties에 자격 증명 설정
#   4. build.gradle에서 version을 Release 버전으로 변경 (예: 1.0.0)
#   5. git tag 생성: git tag -a v1.0.0 -m "Release 1.0.0"
#
# Release 후:
#   1. Sonatype NEXUS UI에서 Staging 저장소를 Close하기
#   2. Close 후 Release하기
#   3. 1~2시간 후 Maven Central에 동기화됨
###############################################################################

set -e

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_DIR"

echo "╔═══════════════════════════════════════════════════════════════╗"
echo "║  Manifesto Core - Deploy Release to Maven Central            ║"
echo "╚═══════════════════════════════════════════════════════════════╝"
echo ""

# 현재 버전 확인
VERSION=$(grep "version = " build.gradle | head -1 | sed "s/.*version = '\([^']*\)'.*/\1/")
echo "📋 버전: $VERSION"
echo ""

# Release 버전 확인
if [[ "$VERSION" == *"SNAPSHOT"* ]] || [[ "$VERSION" == *"-RC"* ]] || [[ "$VERSION" == *"-BETA"* ]]; then
    echo "❌ 오류: Release 배포는 Release 버전(예: 1.0.0)용입니다"
    echo "   현재 버전: $VERSION"
    echo ""
    echo "✅ Release 버전으로 변경하세요:"
    echo "   1. build.gradle의 version을 업데이트"
    echo "   2. git commit -am \"Release $VERSION\""
    echo "   3. git tag -a v$VERSION -m \"Release $VERSION\""
    exit 1
fi

# git 상태 확인
if ! git diff --quiet; then
    echo "❌ 오류: 커밋되지 않은 변경 사항이 있습니다"
    echo "   git status를 확인하고 모든 변경사항을 커밋하세요"
    exit 1
fi

echo "✓ git 상태: 깨끗함"
echo ""

# 자격 증명 확인
if [ -z "$MAVEN_USERNAME" ] && ! grep -q "ossrhUsername" ~/.gradle/gradle.properties 2>/dev/null; then
    echo "❌ 오류: Sonatype OSSRH 자격 증명이 필요합니다"
    echo ""
    echo "~/.gradle/gradle.properties에 다음을 추가하세요:"
    echo "   ossrhUsername=your-jira-username"
    echo "   ossrhPassword=your-jira-password"
    echo "   signing.keyId=YOUR_KEY_ID"
    echo "   signing.password=YOUR_PASSPHRASE"
    exit 1
fi

echo "🔐 자격 증명: 확인됨"
echo ""

# 최종 확인
echo "⚠️  다음을 확인하세요:"
echo "   ✓ 버전: $VERSION"
echo "   ✓ git 상태: 깨끗함"
echo "   ✓ 모든 테스트가 통과했는지"
echo "   ✓ README와 CHANGELOG가 업데이트되었는지"
echo ""
read -p "Release를 진행하시겠습니까? (y/n) " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "❌ Release 취소됨"
    exit 1
fi

echo ""

# 1. 빌드
echo "🔨 빌드 중..."
./gradlew clean build -q
echo "✓ 빌드 완료"
echo ""

# 2. Maven Central에 배포
echo "📤 Sonatype OSSRH에 배포 중..."
echo "   URL: https://oss.sonatype.org/service/local/staging/deploy/maven2/"
echo ""

if [ -n "$MAVEN_USERNAME" ]; then
    ./gradlew publish \
        -PossrhUsername="$MAVEN_USERNAME" \
        -PossrhPassword="$MAVEN_PASSWORD" \
        -q
else
    ./gradlew publish -q
fi

echo "✓ 배포 완료"
echo ""

# 3. git tag 생성 (선택사항)
if [ -z "$SKIP_GIT_TAG" ]; then
    if ! git rev-parse "v$VERSION" >/dev/null 2>&1; then
        echo "🏷️  Git tag 생성 중..."
        git tag -a "v$VERSION" -m "Release $VERSION"
        echo "✓ Tag 생성: v$VERSION"
        echo ""
        echo "💡 다음 명령어로 원격 저장소에 푸시하세요:"
        echo "   git push origin v$VERSION"
        echo ""
    fi
fi

# 4. 배포 확인
echo "✅ Release 배포 성공!"
echo ""
echo "📍 Sonatype NEXUS Staging:"
echo "   https://oss.sonatype.org/#stagingRepositories"
echo ""
echo "⚠️  다음 단계 (필수):"
echo "   1. Sonatype NEXUS에 로그인"
echo "   2. Staging Repositories에서 ai.manifesto 저장소 찾기"
echo "   3. 저장소 선택 후 'Close' 버튼 클릭"
echo "   4. Close 후 'Release' 버튼 클릭"
echo ""
echo "📦 배포 확인 (1~2시간 후):"
echo "   https://repo1.maven.org/maven2/ai/manifesto/manifesto-core/$VERSION/"
echo ""
echo "🎉 사용 예제:"
echo "   dependencies {"
echo "       implementation 'ai.manifesto:manifesto-core:$VERSION'"
echo "   }"
echo ""
echo "📚 자세한 정보: https://central.sonatype.org/publish/"
echo ""
