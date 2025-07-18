## 기술스택 & 라이브러리
- UI 구성 : View + ConstraintLayout
- API 통신 : Retrofit2, Okhttp
- 캘린더 : Kizitonwose
- 네비게이션 : Jetpack Navigation
- 비동기 처리 : Kotlin Coroutines

## Android Studio 환경 설정  
- 안드로이드 스튜디오 Narwhal
- Device: Pixel 9
- minSdk = 26
- targetSdk = 35

## 브랜치 전략
- 기능별로 branch 이름 정함 (feature/login)

## PR 컨벤션
제목 : [이름/#기능] 브랜치1 → 브랜치2 <br>
본문 : 작업 개요 + 변경 사항 + 관련 이슈 + 참고 사항

  
## 이슈 컨벤션 양식 (노션에서 진행)
이슈 + 이슈 상세 내용 + 참고 자료
  
##  Android 네이밍 컨벤션 

**CamelCase + snake_case 혼합 전략**

---

## 1.  기본 원칙

| 요소           | 표기법         | 예시                     |
|----------------|----------------|--------------------------|
| 클래스, 함수, 변수 | `camelCase`, `UpperCamelCase` | `LoginActivity`, `getUserInfo()`, `userName` |
| 리소스 파일명     | `snake_case`   | `activity_login.xml`, `ic_arrow_back.xml` |
| View ID         | `camelCase`    | `btnLogin`, `etPassword` |
| 상수 (`const val`) | `UPPER_SNAKE_CASE` | `MAX_RETRY_COUNT` |

---

## 2.  파일 네이밍

| 종류           | 규칙               | 예시                  |
|----------------|--------------------|-----------------------|
| Activity       | `UpperCamelCase + Activity` | `LoginActivity.kt`     |
| Fragment       | `UpperCamelCase + Fragment` | `SignupFragment.kt`    |
| ViewModel      | `UpperCamelCase + ViewModel`| `MainViewModel.kt`     |
| Adapter        | `UpperCamelCase + Adapter`  | `UserListAdapter.kt`   |
| Layout XML     | `snake_case`       | `activity_main.xml`, `fragment_signup.xml` |
| Drawable/Color | `snake_case`       | `bg_button_green.xml`, `color_primary` |

---

## 3. View ID 네이밍 (XML의 `android:id`)

- 접두어 + 역할을 나타내는 의미 있는 단어 사용  
- **camelCase** 사용

| View 타입    | 접두어 | 예시         |
|--------------|--------|--------------|
| TextView     | `tv`   | `tvTitle`     |
| EditText     | `et`   | `etEmail`     |
| Button       | `btn`  | `btnSubmit`   |
| ImageView    | `iv`   | `ivProfile`   |
| RecyclerView | `rv`   | `rvItemList`  |
| ConstraintLayout | `cl` | `clContainer` |

---

## 4. 리소스 이름 규칙

| 종류         | 형식        | 예시                  |
|--------------|-------------|-----------------------|
| Layout       | `snake_case`| `activity_login.xml`  |
| Drawable     | `snake_case`| `btn_primary_bg.xml`  |
| Color        | `snake_case`| `color_primary`       |
| String       | `snake_case`| `login_error_msg`     |
| Style        | `UpperCamelCase`| `LoginButtonStyle`|

---

## 5. 상수 (const val, static final)

- 모두 대문자 + 밑줄 (`UPPER_SNAKE_CASE`) 사용  
```kotlin
const val DEFAULT_TIMEOUT = 5000
const val MAX_RETRY_COUNT = 3
```

---

## 6. 기타 권장 사항

- **의미 없는 약어 지양**: `tvMsg1` ❌ → `tvLoginMessage` ✅  
- **UI 요소는 역할을 명확히 표현**: `btnSubmit`보단 `btnLogin`, `btnSignUp`  
- 공용 리소스 파일 (strings.xml 등) 수정 시 팀원에게 공유 필수


  ## color, font

<img width="765" height="639" alt="Image" src="https://github.com/user-attachments/assets/50dd2802-1431-4785-b6be-ce5276a77c0b" />
<img width="985" height="357" alt="Image" src="https://github.com/user-attachments/assets/789183bb-e90f-4962-bd84-ebbc581cafb1" />

---
최종 수정일 : 25.7.11


