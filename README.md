<div align="center">
  <a>
    <img width="115" src="https://github.com/user-attachments/assets/8f2deb82-28ad-49f5-b442-73c0902dd4e7">
  </a>
  <h3 align="center">나의 펫이 되어줘</h3>
  <p align="center">
    유기동물 보호소의 유기동물 조회 어플리케이션
    <div style=" padding-bottom: 1rem;">
      <img src="https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" />
      <img src="https://img.shields.io/badge/Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white">    
      <img src="https://img.shields.io/badge/Kotlin-B125EA?style=for-the-badge&logo=kotlin&logoColor=white" />
    </div>
  </p>
</div>

<a align="center" href="https://play.google.com/store/apps/details?id=kr.sjh.bemypet">
    <img src="https://i.namu.wiki/i/eRJ4F1pIk_HbZWgegNDAhu3jYgb-YbyB7vB4sgBLd1WrthI4VRS4lqothtJE9JdxE1zmeIHIhgYWeL_DmPy-CRd4-FK4PI5IWfOfbhAnjijNvdXINJcG20DLU2nw7e5E5wxCT2YrqQL0odpSnDbE8w.svg"/>
</a>

## Features

- 유기동물 목록 조회
- 유기동물 상세정보 조회 
- '찜'을 누른 동물 조회
- 유기동물 사진 확대 / 축소
- 원하는 조건으로 필터 기능

## Tech Stacks

- MVVM
- Jetpack Compose
- Navigation Compose
- Dependency Injection Hilt
- HTTP 클라이언트 라이브러리 Ktro
- 이미지 로딩 라이브러리 Coil

## API
- [ 농림축산식품부 농림축산검역본부_국가동물보호정보시스템 구조동물 조회 서비스](https://www.data.go.kr/data/15098931/openapi.do)

## Getting Started

### Build

로컬/워크트리 빌드 시 아래 비추적 파일이 필요합니다.
- `secrets.dev.properties`
- `secrets.prod.properties`
- `secrets.properties`
- `version.properties`
- `app/src/dev/google-services.json`
- `app/src/prod/google-services.json`

새 worktree를 만들었을 때는 아래 명령으로 기본 워크스페이스의 로컬 파일을 동기화하세요.

```bash
./scripts/bootstrap-worktree-local.sh
```

이미 있는 파일까지 덮어쓰려면 아래처럼 실행합니다.

```bash
./scripts/bootstrap-worktree-local.sh --force
```

기본 소스 루트 대신 다른 경로에서 가져오려면 환경변수를 사용하세요.

```bash
BEMYPET_LOCAL_SOURCE_ROOT=/path/to/source ./scripts/bootstrap-worktree-local.sh
```

워크트리를 자주 만든다면 아래 전역 alias를 등록해두면 `worktree add + 로컬 파일 동기화`를 한 번에 처리할 수 있습니다.

```bash
git config --global alias.wtb '!f(){ set -e; branch="$1"; if [ -z "$branch" ]; then echo "usage: git wtb <branch> [base=origin/develop] [path]"; exit 1; fi; base="${2:-origin/develop}"; repo="$(git rev-parse --show-toplevel)" || exit 1; slug="$(printf "%s" "$branch" | tr "/" "-")"; if [ -n "${3:-}" ]; then case "$3" in /*) wt="$3" ;; *) wt="$repo/$3" ;; esac; else wt="$repo/.worktrees/$slug"; fi; if [ -e "$wt" ]; then echo "worktree path already exists: $wt"; exit 1; fi; if git -C "$repo" show-ref --verify --quiet "refs/heads/$branch"; then git -C "$repo" worktree add "$wt" "$branch"; else git -C "$repo" worktree add -b "$branch" "$wt" "$base"; fi; copy_if_exists(){ src="$1"; dst="$2"; if [ -f "$src" ] && [ ! -f "$dst" ]; then mkdir -p "$(dirname "$dst")"; cp "$src" "$dst"; echo "copied: $dst"; fi; }; if [ -x "$wt/scripts/bootstrap-worktree-local.sh" ]; then "$wt/scripts/bootstrap-worktree-local.sh"; else copy_if_exists "$repo/secrets.dev.properties" "$wt/secrets.dev.properties"; copy_if_exists "$repo/secrets.prod.properties" "$wt/secrets.prod.properties"; copy_if_exists "$repo/secrets.properties" "$wt/secrets.properties"; copy_if_exists "$repo/version.properties" "$wt/version.properties"; copy_if_exists "$repo/app/src/dev/google-services.json" "$wt/app/src/dev/google-services.json"; copy_if_exists "$repo/app/src/prod/google-services.json" "$wt/app/src/prod/google-services.json"; fi; echo "ready: $wt"; }; f'
```

사용 예시:

```bash
git wtb feature/your-branch
```

### ScreenShot

<img alt="Screenshot_20241211_192307" height="200" src="https://github.com/user-attachments/assets/bbb0c033-0c2e-4973-8d44-05543b1e7adf" width="100"/>
<img alt="Screenshot_20241211_192252" height="200" src="https://github.com/user-attachments/assets/b9af7069-fb4f-4589-a114-a3dc6de7756c" width="100"/>
<img alt="Screenshot_20241211_192232" height="200" src="https://github.com/user-attachments/assets/1a5d4fe7-210a-4f8f-8a4f-20412d479f83" width="100"/>
<img alt="Screenshot_20241211_192152" height="200" src="https://github.com/user-attachments/assets/3c4a4840-c9ca-4326-a988-499da15dd311" width="100"/>
<img alt="Screenshot_20241211_192129" height="200" src="https://github.com/user-attachments/assets/4bf1f014-84a1-4e67-b754-d790aadea9da" width="100"/>
<img alt="Screenshot_20241211_192106" height="200" src="https://github.com/user-attachments/assets/39b496c7-83fd-4fd9-839f-cc76e2521705" width="100"/>
<img alt="Screenshot_20241211_192339" height="200" src="https://github.com/user-attachments/assets/9db55e9d-7bf0-43b8-8cac-19c8dd391286" width="100"/>

### Demo 

https://github.com/user-attachments/assets/c3b4cbfa-d73d-4b5e-b3c1-0edf841efff7


