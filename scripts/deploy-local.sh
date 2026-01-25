#!/bin/bash

###############################################################################
# deploy-local.sh - 로컬 Maven Repository로 배포
#
# 사용법: ./scripts/deploy-local.sh
#
# 설명:
#   - 프로젝트를 빌드합니다
#   - 로컬 Maven 캐시 (~/.m2/repository)에 배포합니다
#   - 개발 및 테스트용으로 권장됩니다
###############################################################################

set -e

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_DIR"

echo "╔═══════════════════════════════════════════════════════════════╗"
echo "║  Manifesto Core - Deploy to Local Maven Repository           ║"
echo "╚═══════════════════════════════════════════════════════════════╝"
echo ""

# 현재 버전 확인
VERSION=$(grep "version = " build.gradle | head -1 | sed "s/.*version = '\([^']*\)'.*/\1/")
echo "📋 버전: $VERSION"
echo ""

# 1. 빌드
echo "🔨 빌드 중..."
./gradlew clean build -q
echo "✓ 빌드 완료"
echo ""

# 2. 로컬 Maven Repository에 배포
echo "📦 로컬 Maven Repository에 배포 중..."
./gradlew publishToMavenLocal -q
echo "✓ 배포 완료"
echo ""

# 3. 배포 확인
echo "✅ 배포 성공!"
echo ""
echo "📍 설치 위치:"
MAVEN_REPO="$HOME/.m2/repository/ai/manifesto/manifesto-core/$VERSION"
if [ -d "$MAVEN_REPO" ]; then
    ls -lh "$MAVEN_REPO"
else
    echo "⚠️  배포 위치를 찾을 수 없습니다"
    exit 1
fi

echo ""
echo "🎉 이제 로컬 프로젝트에서 다음과 같이 사용할 수 있습니다:"
echo "   dependencies {"
echo "       implementation 'ai.manifesto:manifesto-core:$VERSION'"
echo "   }"
echo ""
