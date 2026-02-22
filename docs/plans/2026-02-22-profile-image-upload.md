# Profile Image Upload Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 설정 화면 프로필 수정에서 아바타 URL 수동 입력을 제거하고, 갤러리 이미지 업로드 결과 URL로 프로필 이미지를 갱신한다.

**Architecture:** 설정 UI는 이미지 선택/전처리(512x512 JPEG 80)를 담당하고, 데이터 계층은 Supabase Storage(`profile-images/{userId}/avatar.jpg`) 업로드와 프로필 업데이트를 담당한다. 업로드 성공 시에만 `profiles.avatar_url`를 갱신하며, 실패 시 업데이트를 중단하고 재시도 가능한 UX를 유지한다.

**Tech Stack:** Android Photo Picker (`ActivityResultContracts.PickVisualMedia`), Kotlin Coroutines, Supabase-kt (`auth-kt`, `postgrest-kt`, `storage-kt`), Coil Compose, JUnit4

---

### Task 1: Storage 업로드 계약 추가 (Repository/Service 레이어)

**Files:**
- Modify: `/Users/junghoon/AndroidStudioProjects/BeMyPet/gradle/libs.versions.toml`
- Modify: `/Users/junghoon/AndroidStudioProjects/BeMyPet/core/supabase/build.gradle.kts`
- Modify: `/Users/junghoon/AndroidStudioProjects/BeMyPet/core/data/src/main/java/kr/sjh/data/repository/AuthRepository.kt`
- Modify: `/Users/junghoon/AndroidStudioProjects/BeMyPet/core/data/src/main/java/kr/sjh/data/repository/impl/AuthRepositoryImpl.kt`
- Modify: `/Users/junghoon/AndroidStudioProjects/BeMyPet/core/supabase/src/main/java/kr/sjh/core/supabase/service/AuthService.kt`
- Test: `/Users/junghoon/AndroidStudioProjects/BeMyPet/core/data/src/test/java/kr/sjh/data/repository/impl/AuthRepositoryImplUploadAvatarTest.kt`

**Step 1: Write the failing test**

```kotlin
@Test
fun uploadProfileAvatar_delegatesToAuthService() = runTest {
    val fake = FakeAuthService()
    val repo = AuthRepositoryImpl(fake)
    val bytes = byteArrayOf(1, 2, 3)

    val url = repo.uploadProfileAvatar("user-id", bytes, "image/jpeg")

    assertEquals("https://example/avatar.jpg", url)
    assertEquals("user-id", fake.lastUserId)
    assertContentEquals(bytes, fake.lastBytes)
}
```

**Step 2: Run test to verify it fails**

Run: `./gradlew :core:data:testDebugUnitTest --tests "*AuthRepositoryImplUploadAvatarTest" --no-daemon`  
Expected: FAIL (`Unresolved reference: uploadProfileAvatar`)

**Step 3: Write minimal implementation**

```kotlin
// AuthRepository.kt
suspend fun uploadProfileAvatar(userId: String, bytes: ByteArray, contentType: String = "image/jpeg"): String

// AuthRepositoryImpl.kt
override suspend fun uploadProfileAvatar(userId: String, bytes: ByteArray, contentType: String): String {
    return authService.uploadProfileAvatar(userId, bytes, contentType)
}

// AuthService.kt
suspend fun uploadProfileAvatar(userId: String, bytes: ByteArray, contentType: String = "image/jpeg"): String
```

**Step 4: Run test to verify it passes**

Run: `./gradlew :core:data:testDebugUnitTest --tests "*AuthRepositoryImplUploadAvatarTest" --no-daemon`  
Expected: PASS

**Step 5: Commit**

```bash
git add /Users/junghoon/AndroidStudioProjects/BeMyPet/gradle/libs.versions.toml \
  /Users/junghoon/AndroidStudioProjects/BeMyPet/core/supabase/build.gradle.kts \
  /Users/junghoon/AndroidStudioProjects/BeMyPet/core/data/src/main/java/kr/sjh/data/repository/AuthRepository.kt \
  /Users/junghoon/AndroidStudioProjects/BeMyPet/core/data/src/main/java/kr/sjh/data/repository/impl/AuthRepositoryImpl.kt \
  /Users/junghoon/AndroidStudioProjects/BeMyPet/core/supabase/src/main/java/kr/sjh/core/supabase/service/AuthService.kt \
  /Users/junghoon/AndroidStudioProjects/BeMyPet/core/data/src/test/java/kr/sjh/data/repository/impl/AuthRepositoryImplUploadAvatarTest.kt
git commit -m "feat(profile): add avatar upload contract in auth repository"
```

### Task 2: Supabase Storage 업로드 구현 (AuthServiceImpl)

**Files:**
- Modify: `/Users/junghoon/AndroidStudioProjects/BeMyPet/core/supabase/src/main/java/kr/sjh/core/supabase/service/impl/AuthServiceImpl.kt`
- Test: `/Users/junghoon/AndroidStudioProjects/BeMyPet/core/supabase/src/test/java/kr/sjh/core/supabase/service/impl/AuthServiceImplAvatarPathTest.kt`

**Step 1: Write the failing test**

```kotlin
@Test
fun buildAvatarObjectPath_usesStableUserScopedPath() {
    assertEquals("abc/avatar.jpg", buildAvatarObjectPath(" abc "))
}
```

**Step 2: Run test to verify it fails**

Run: `./gradlew :core:supabase:testDebugUnitTest --tests "*AuthServiceImplAvatarPathTest" --no-daemon`  
Expected: FAIL (`Unresolved reference: buildAvatarObjectPath`)

**Step 3: Write minimal implementation**

```kotlin
override suspend fun uploadProfileAvatar(userId: String, bytes: ByteArray, contentType: String): String {
    val normalizedUserId = userId.trim()
    require(normalizedUserId.isNotBlank()) { "userId is blank" }
    require(bytes.isNotEmpty()) { "avatar bytes are empty" }

    val objectPath = buildAvatarObjectPath(normalizedUserId)
    val bucket = client.storage.from("profile-images")
    bucket.upload(path = objectPath, data = bytes) {
        upsert = true
        this.contentType = contentType
    }
    return bucket.publicUrl(objectPath)
}

internal fun buildAvatarObjectPath(userId: String): String = "${userId.trim()}/avatar.jpg"
```

**Step 4: Run test to verify it passes**

Run: `./gradlew :core:supabase:testDebugUnitTest --tests "*AuthServiceImplAvatarPathTest" --no-daemon`  
Expected: PASS

**Step 5: Commit**

```bash
git add /Users/junghoon/AndroidStudioProjects/BeMyPet/core/supabase/src/main/java/kr/sjh/core/supabase/service/impl/AuthServiceImpl.kt \
  /Users/junghoon/AndroidStudioProjects/BeMyPet/core/supabase/src/test/java/kr/sjh/core/supabase/service/impl/AuthServiceImplAvatarPathTest.kt
git commit -m "feat(profile): upload avatar image to supabase storage"
```

### Task 3: ViewModel 오케스트레이션 추가 (upload -> updateProfile)

**Files:**
- Modify: `/Users/junghoon/AndroidStudioProjects/BeMyPet/feature/setting/src/main/java/kr/sjh/setting/screen/SettingViewModel.kt`
- Test: `/Users/junghoon/AndroidStudioProjects/BeMyPet/feature/setting/src/test/java/kr/sjh/setting/screen/SettingViewModelProfileUploadTest.kt`

**Step 1: Write the failing test**

```kotlin
@Test
fun updateProfileWithAvatar_uploadsFirst_thenUpdatesProfile() = runTest {
    val fakeRepo = FakeAuthRepository()
    val vm = SettingViewModel(fakeRepo)

    vm.updateProfileWithAvatar(
        userId = "user-id",
        displayName = "new-name",
        avatarBytes = byteArrayOf(1, 2),
        onSuccess = {},
        onFailure = {}
    )

    assertEquals(listOf("upload", "update"), fakeRepo.callOrder)
}
```

**Step 2: Run test to verify it fails**

Run: `./gradlew :feature:setting:testDebugUnitTest --tests "*SettingViewModelProfileUploadTest" --no-daemon`  
Expected: FAIL (`Unresolved reference: updateProfileWithAvatar`)

**Step 3: Write minimal implementation**

```kotlin
fun updateProfileWithAvatar(
    userId: String,
    displayName: String,
    avatarBytes: ByteArray?,
    onSuccess: () -> Unit,
    onFailure: (Exception) -> Unit,
) {
    viewModelScope.launch {
        runCatching {
            val avatarUrl = if (avatarBytes != null) {
                authRepository.uploadProfileAvatar(userId, avatarBytes, "image/jpeg")
            } else {
                profileUiState.value.profile?.avatarUrl
            }
            authRepository.updateProfile(userId, displayName, avatarUrl, onSuccess, onFailure)
        }.onFailure { onFailure(it as? Exception ?: Exception(it)) }
    }
}
```

**Step 4: Run test to verify it passes**

Run: `./gradlew :feature:setting:testDebugUnitTest --tests "*SettingViewModelProfileUploadTest" --no-daemon`  
Expected: PASS

**Step 5: Commit**

```bash
git add /Users/junghoon/AndroidStudioProjects/BeMyPet/feature/setting/src/main/java/kr/sjh/setting/screen/SettingViewModel.kt \
  /Users/junghoon/AndroidStudioProjects/BeMyPet/feature/setting/src/test/java/kr/sjh/setting/screen/SettingViewModelProfileUploadTest.kt
git commit -m "feat(setting): orchestrate avatar upload before profile update"
```

### Task 4: 설정 다이얼로그 UI를 이미지 선택 기반으로 전환

**Files:**
- Modify: `/Users/junghoon/AndroidStudioProjects/BeMyPet/feature/setting/src/main/java/kr/sjh/setting/screen/SettingScreen.kt`
- Create: `/Users/junghoon/AndroidStudioProjects/BeMyPet/feature/setting/src/main/java/kr/sjh/setting/screen/AvatarImageCompressor.kt`
- Test: `/Users/junghoon/AndroidStudioProjects/BeMyPet/feature/setting/src/test/java/kr/sjh/setting/screen/AvatarImageCompressorTest.kt`

**Step 1: Write the failing test**

```kotlin
@Test
fun avatarFileName_isStable() {
    assertEquals("avatar.jpg", AvatarImageCompressor.TARGET_FILE_NAME)
}
```

**Step 2: Run test to verify it fails**

Run: `./gradlew :feature:setting:testDebugUnitTest --tests "*AvatarImageCompressorTest" --no-daemon`  
Expected: FAIL (`Unresolved reference: AvatarImageCompressor`)

**Step 3: Write minimal implementation**

```kotlin
// AvatarImageCompressor.kt
object AvatarImageCompressor {
    const val TARGET_SIZE = 512
    const val TARGET_FILE_NAME = "avatar.jpg"
    const val JPEG_QUALITY = 80

    fun compress(contentResolver: ContentResolver, uri: Uri): ByteArray {
        // decode -> centerCrop(512x512) -> JPEG(80)
    }
}

// SettingScreen.kt
// - avatar URL 입력 필드 제거
// - PickVisualMedia 런처 추가
// - 이미지 미리보기 + "이미지 선택" 버튼 추가
// - onSave 시 avatarBytes를 SettingViewModel.updateProfileWithAvatar로 전달
```

**Step 4: Run tests and compile**

Run:
- `./gradlew :feature:setting:testDebugUnitTest --no-daemon`
- `./gradlew :app:compileDevDebugKotlin --no-daemon`

Expected: PASS + BUILD SUCCESSFUL

**Step 5: Commit**

```bash
git add /Users/junghoon/AndroidStudioProjects/BeMyPet/feature/setting/src/main/java/kr/sjh/setting/screen/SettingScreen.kt \
  /Users/junghoon/AndroidStudioProjects/BeMyPet/feature/setting/src/main/java/kr/sjh/setting/screen/AvatarImageCompressor.kt \
  /Users/junghoon/AndroidStudioProjects/BeMyPet/feature/setting/src/test/java/kr/sjh/setting/screen/AvatarImageCompressorTest.kt
git commit -m "feat(setting): replace avatar url input with gallery image upload"
```

### Task 5: 최종 검증 및 문서 반영

**Files:**
- Modify: `/Users/junghoon/AndroidStudioProjects/BeMyPet/docs/plans/2026-02-22-profile-image-upload-design.md` (필요 시 구현 결과 반영)

**Step 1: Run full relevant tests**

Run:
- `./gradlew :core:data:testDebugUnitTest :core:supabase:testDebugUnitTest :feature:setting:testDebugUnitTest --no-daemon`

Expected: PASS

**Step 2: Manual E2E verification**

Run checklist:
1. 설정 > 프로필 수정 > 이미지 선택 > 저장
2. 댓글/설정 등 다른 화면 이동 후 아바타 유지 확인
3. 앱 재시작 후 아바타 유지 확인
4. 네트워크 차단 후 저장 시 실패 메시지 + 다이얼로그 유지 확인

Expected: 모든 시나리오 정상

**Step 3: Commit**

```bash
git add /Users/junghoon/AndroidStudioProjects/BeMyPet/docs/plans/2026-02-22-profile-image-upload-design.md
git commit -m "docs: finalize profile image upload validation notes"
```

---

Execution note: Use `@superpowers:executing-plans` for strict task-by-task execution and checkpoint review.
