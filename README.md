## 기술스택 & 라이브러리
- UI 구성 : View + ConstraintLayout
- API 통신 : Retrofit2, Okhttp
- 캘린더 : Kizitonwose
- 네비게이션 : Jetpack Navigation
- 비동기 처리 : Kotlin Coroutines

## Android Studio 환경 설정  
- 안드로이드 스튜디오 미어캣
- Device: Pixel 9
- minSdk = 26
- targetSdk = 35

## 브랜치 전략
- 기능별로 branch 이름 정함 (feature/login)

## PR 컨벤션
# 제목 규칙
[이름/#기능] 브랜치1 → 브랜치2

# 본문 규칙
##  작업 개요
- ex) 로그인 화면 구성

##  변경 사항
- ex) activity_login.xml 작성

##  관련 이슈
- resolved: #12

##  참고 사항
- ex) Figma 디자인 기반으로 레이아웃 구성


  
## 이슈 컨벤션 양식 (노션에서 진행)
  
 ## 이슈
- 어떤 문제가 발생했는지

## 이슈 상세 내용(ex)
- [ ] 레이아웃 구성 중 오류
- [ ] 버튼 클릭 안됨.
- [ ] Navigation 연결 처리 안됨

## 참고 자료
- 관련 PR: #12
<hr

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
- **UI 요소는 역할을 명확히 표현**: `btnSubmit`보단 `btnLogin`, `btnSignUp`이 더 구체적  
- **ViewBinding 기반 프로젝트라면 camelCase를 기준으로 정리**  
- 공용 리소스 파일 (strings.xml 등) 수정 시 팀원에게 공유 필수


  ## color, font

![image.png](attachment:7f700b91-96d3-4d4f-8c79-f07a417c04b4:image.png)
![image.png](attachment:9db0b061-dd83-461e-acdc-0db720387279:image.png)

---
최종 수정일 : 25.7.11


