#!/bin/bash

###############################################################################
# deploy-snapshot.sh - Maven Central (Sonatype OSSRH)에 SNAPSHOT 배포
#
# 사용법: MAVEN_USERNAME=user MAVEN_PASSWORD=pass ./scripts/deploy-snapshot.sh
#         또는 ~/.gradle/gradle.properties에 자격 증명 설정
#
# 사전 준비:
#   1. Sonatype OSSRH 계정 생성: https://central.sonatype.org/publish/
#   2. GPG 키 생성: gpg --gen-key
#   3. GPG 키 서버에 등록: gpg --keyserver keyserver.ubuntu.com --send-keys YOUR_KEY_ID
#   4. ~/.gradle/gradle.properties 설정:
#      - ossrhUsername=your-jira-username
#      - ossrhPassword=your-jira-password
#      - signing.keyId=YOUR_KEY_ID
#      - signing.password=YOUR_PASSPHRASE
###############################################################################

set -e

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_DIR"

echo "╔═══════════════════════════════════════════════════════════════╗"
echo "║  Manifesto Core - Deploy SNAPSHOT to Maven Central           ║"
echo "╚═══════════════════════════════════════════════════════════════╝"
echo ""

# 현재 버전 확인
VERSION=$(grep "version = " build.gradle | head -1 | sed "s/.*version = '\([^']*\)'.*/\1/")
echo "📋 버전: $VERSION"
echo ""

# SNAPSHOT 버전 확인
if [[ ! "$VERSION" == *"SNAPSHOT"* ]]; then
    echo "⚠️  경고: 현재 버전이 SNAPSHOT이 아닙니다 ($VERSION)"
    echo "   SNAPSHOT 배포는 개발 버전(-SNAPSHOT)용입니다"
    read -p "계속하시겠습니까? (y/n) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "❌ 배포 취소됨"
        exit 1
    fi
fi

# 자격 증명 확인
if [ -z "$MAVEN_USERNAME" ] && ! grep -q "ossrhUsername" ~/.gradle/gradle.properties 2>/dev/null; then
    echo "❌ 오류: Sonatype OSSRH 자격 증명이 필요합니다"
    echo ""
    echo "다음 중 하나를 선택하세요:"
    echo "1. 환경 변수 설정:"
    echo "   export MAVEN_USERNAME=your-jira-username"
    echo "   export MAVEN_PASSWORD=your-jira-password"
    echo "   ./scripts/deploy-snapshot.sh"
    echo ""
    echo "2. ~/.gradle/gradle.properties에 설정:"
    echo "   ossrhUsername=your-jira-username"
    echo "   ossrhPassword=your-jira-password"
    echo ""
    exit 1
fi

echo "🔐 자격 증명: 확인됨"
echo ""

# 1. 빌드
echo "🔨 빌드 중..."
./gradlew clean build -q
echo "✓ 빌드 완료"
echo ""

# 2. Maven Central에 배포
echo "📤 Sonatype OSSRH에 배포 중..."
echo "   URL: https://oss.sonatype.org/content/repositories/snapshots/"
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

# 3. 배포 확인
echo "✅ SNAPSHOT 배포 성공!"
echo ""
echo "📍 Maven Central Snapshot 저장소:"
echo "   https://oss.sonatype.org/content/repositories/snapshots/ai/manifesto/manifesto-core/$VERSION"
echo ""
echo "🎉 이제 다른 프로젝트에서 다음과 같이 사용할 수 있습니다:"
echo "   repositories {"
echo "       maven {"
echo "           url 'https://oss.sonatype.org/content/repositories/snapshots/'"
echo "       }"
echo "   }"
echo "   dependencies {"
echo "       implementation 'ai.manifesto:manifesto-core:$VERSION'"
echo "   }"
echo ""
echo "📚 자세한 정보: https://central.sonatype.org/publish/"
echo ""
