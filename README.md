# 자바 WAS 만들기

## 1. 미션 목록

### 요구사항 1

- http://localhost:8080/index.html 로 접속했을 때 webapp 디렉토리의 index.html 파일을 읽어 클라이언트에 응답한다.

### 요구사항 2

- “회원가입” 메뉴를 클릭하면 http://localhost:8080/user/form.html 으로 이동하면서 회원가입할 수 있다. 회원가입한다.
- 회원가입을 하면 다음과 같은 형태로 사용자가 입력한 값이 서버에 전달된다.
- HTML과 URL을 비교해 보고 사용자가 입력한 값을 파싱해 model.User 클래스에 저장한다.

### 요구사항 3

- http://localhost:8080/user/form.html 파일의 form 태그 method를 get에서 post로 수정한 후 회원가입 기능이 정상적으로 동작하도록 구현한다.

### 요구사항 4

- “회원가입”을 완료하면 /index.html 페이지로 이동하고 싶다. 현재는 URL이 /user/create 로 유지되는 상태로 읽어서 전달할 파일이 없다. 따라서 redirect 방식처럼 회원가입을 완료한 후
  “index.html”로 이동해야 한다. 즉, 브라우저의 URL이 /index.html로 변경해야 한다.

### 요구사항 5

- “로그인” 메뉴를 클릭하면 http://localhost:8080/user/login.html 으로 이동해 로그인할 수 있다. 로그인이 성공하면 index.html로 이동하고, 로그인이 실패하면
  /user/login_failed.html로 이동해야 한다.
- 앞에서 회원가입한 사용자로 로그인할 수 있어야 한다. 로그인이 성공하면 cookie를 활용해 로그인 상태를 유지할 수 있어야 한다. 로그인이 성공할 경우 요청 header의 Cookie header 값이
  logined=true, 로그인이 실패하면 Cookie header 값이 logined=false로 전달되어야 한다.

### 요구사항 6

- 접근하고 있는 사용자가 “로그인” 상태일 경우(Cookie 값이 logined=true) 경우 http://localhost:8080/user/list 로 접근했을 때 사용자 목록을 출력한다. 만약 로그인하지
  않은 상태라면 로그인 페이지(login.html)로 이동한다.

### 요구사항 7

- 지금까지 구현한 소스 코드는 stylesheet 파일을 지원하지 못하고 있다. Stylesheet 파일을 지원하도록 구현하도록 한다.

## 2. 구현 특징

### 특징 1 : 코드 흐름

각각의 요청당 하나의 스레드가 생성되고(현제 thread pool을 구현하는것은 아니니...), 해당 스레드가 Servlet의 service를 호출한다는 개념에 착안하여 구현하게 되었습니다. 따라서 각 요청을
Handler에서 처리하여, 미리 생성된 Servlet을 불러 호출한다 생각하였습니다.

기존 Servlet의 요청방식과 저희 팀의 구현을 좀 비교해 보면

1. 사용자(클라이언트)가 URL을 통해 요청을 보내면 HTTP Request를 Servlet Conatiner로 전송합니다. (저희의WebServer.class 또한 요청을 기다리다 요청이 오면 연결합니다)
2. HTTP Request를 전송받은 Servlet Container는 HttpServletRequest, HttpServletResponse 두 객체를 생성합니다. (저희의 request, response 또한
   두 객체를 우선적으로 생성합니다)
3. 그 다음에는 요청한 URL을 분석하여 어느 서블릿에 대해 요청을 한 것인지 찾습니다. (언급하신 controlServlet 부분에서 요청할 서블릿을 찾게 됩니다)
4. 해당 서블릿에서 service메소드를 호출한 후 POST, GET여부에 따라 doGet() 또는 doPost()를 호출합니다. (저희의가 구현한 Servlet 또한 service를 호출하면 내부적으로 get,
   post 를 구분하여 호출하게 됩니다)
5. doGet() or doPost() 메소드는 동적 페이지를 생성한 후 HttpServletResponse객체에 응답을 보냅니다. (저희팀 또한 이후 요청 페이지를 반환하게 됩니다)

### 특징 1 : Servlet의 구현

Servlet 이라는 대표적인 interface를 다음과 같이 구현하였습니다.

```java
public interface Servlet {
    public void service(HttpRequest request, HttpResponse response);
}
```

이를 구현하는 BaseServlet의 코드는 다음과 같습니다.

```java
public class BaseServlet implements Servlet {
    @Override
    public void service(HttpRequest request, HttpResponse response) {
      String method = request.getMethod();
      if (method.equals("GET")) {
        doGet(request, response);
      } else {
        doPost(request, response);
      }
    }
    
    public void doGet(HttpRequest request, HttpResponse response) {};
    public void doPost(HttpRequest request, HttpResponse response) {};
}
```

기본적인 BaseServlet을 만든 후, 각 servlet들이 이에 맞도록 자식 class에서 구현하도록 하였습니다. 사용 하는 입장에서는 servlet.service()만 호출하면 됩니다.

다이어그렘으로 살펴보면 다음과 같습니다.
<img width="1044" alt="스크린샷 2022-04-02 오후 2 51 55" src="https://user-images.githubusercontent.com/60593969/161368669-564c3331-3750-49ec-97be-c08e688baa23.png">

### 특징 2 : Response, Request 객체 생성

처음 inputStream 으로 들어오는 데이터들을 Request 객체로 만들었으며, 작업이 진행되며 출력해야 되는 데이터들을 Response 객체로 만들게 되었습니다.

### 특징 3 : Annotation을 활용한 Servlet Mapping 하기

원래는 사전에 미리 servlet 과 호출될 url을 다음과 같이 하나하나 mapping 하여 추가했습니다.

```java
static {
    servletMap.put("/user/create",new CreateUserServlet());
    servletMap.put("/user/login",new LoginServlet());
    servletMap.put("/user/logout",new LogoutServlet());
}
```

하지만 이런식으로 하나하나 Servlet을 만들 때 마다 처리하기는 힘들다 생각하여 @MyServletMapping 이라는 에노테이션을 만들게 되었습니다.

```java

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyServletMapping { 
    String url();
}
```

위 에노테이션을 작성한 Servlet 위에 다음과 같은 형식으로 url 을 지정하여 추가해 줍니다.

```java
@MyServletMapping(url = "/user/create")
```

이를 Reflection을 활용하여 로딩하여 map에 등록하게 되었습니다. 코드는 다음과 같습니다.

```java
Reflections reflector=new Reflections("servlet");
        addServletToMap(reflector);

private static void addServletToMap(Reflections reflector)throws InstantiationException,IllegalAccessException,InvocationTargetException,NoSuchMethodException{
    Set<Class<?>> list = reflector.getTypesAnnotatedWith(MyServletMapping.class);
    for(Class<?> clazz:list){
        String url=clazz.getAnnotation(MyServletMapping.class).url();
        Servlet servlet=(Servlet)clazz.getDeclaredConstructor().newInstance();
        ServletMap.addServlet(url,servlet);
    }
}
```

이렇게 ServletMap에 등록시킨 후, 사용할때 map을 통해 mapping해오게 됩니다.

### 특징 4 : LoginFilter 구현하기

우선 Filter Interface를 다음과 같이 구현하였습니다.

```java
public interface Filter { 
    public boolean doFilter(HttpRequest request, HttpResponse response);
}
```

이를 구현한 LoginFilter는 다음과 같습니다. 코드가 조금 복잡해지기는 했는데, 사용하기는 편해졌습니다. LoginFilter.addUrl 을 통해 로그인 검증을 하고싶은 url을 지정만 하면 검증하게
되었습니다.

```java
public class LoginFilter implements Filter {
  private static final Logger log = LoggerFactory.getLogger(LoginFilter.class);
  private List<String> redLabel = new ArrayList<>();

  @Override
  public boolean doFilter(HttpRequest request, HttpResponse response) {
    // 해당 url인지 검사
    if (isInaccessible(request.getPath())) { // 로그인 체크 해야 하는곳
      return isLoginedUser(request, response);
    }
    return true;
  }

  private boolean isLoginedUser(HttpRequest request, HttpResponse response) {
    if (isValidSession(request)) return true;
    // 로그인 화면으로 이동
    response.forward("/user/login.html");
    return false;
  }

  private boolean isValidSession(HttpRequest request) {
    String sessionId = getSessionId(request);
    if (sessionId != null) {
      if (Session.isLoginUser(sessionId)) {
        log.debug("[LoginFilter] {}", sessionId);
        return true;
      }
    }
    return false;
  }

  private String getSessionId(HttpRequest request) {
    Map<String, String> headers = request.getHeaders();
    String values = headers.get("Cookie");
    String[] splits = values.split("; ");
    if (splits.length > 1) {
      return splits[1].substring(10);
    }
    return null;
  }

  public void addUrl(String url) {
    this.redLabel.add(url);
  }

  private boolean isInaccessible(String requestURI) {
    return redLabel.stream()
            .filter(url -> url.equals(requestURI))
            .findFirst()
            .isPresent();
  }
}
```

## Step06 TODO list

- [x] 글 작성
  - [x] 글 쓰기 Form 불러오기
  - [x] 작성된 글 저장하기
  - [x] 로그인 필터처리
- [x] 게시물 list 출력
- [x] CSS 적용하기

## Step04 리뷰 반영

- [x] DataBase.findUserById "ddd" 수정하기
- [x] Session DB에서 직접 세션 생성하도록 변경
- [x] LogoutServlet.getSessionId에서 상수 사용하기 (SESSION_ID_IDX=1)
- [ ] HttpResponse 클래스 코드 리팩토링 하기

## Step05 TODO list

- [x] 로그인 상태일 경우 User 목록 출력
- [x] 로그인 하지 않은 상태면 Login으로 이동
- [x] LoginFilter 리팩토링 하기다

## Step04 TODO list

- [x] Servlet interface 만들기
- [x] CreateUserServlet (내부로 createUser 메서드 이동시키기)
- [x] 로그인 기능 구현 (http://localhost:8080/user/login.html)
  - [x] 성공시 index.html로 이동
  - [x] 실패시 /user/login_failed.html로 이동

## Step03 리뷰 반영

- [x] Database 유저 중복 관련해서 IllegalArgumentException 아닌 다른 Exception으로 변경
- [x] Database.addUser 리턴 제거하기
- [x] HttpRequest의 header 명칭 변경
- [x] HttpResponse 이름 고민해보기 -> 향후에 복잡해지면 변경
- [x] createRequest 이름 변경하기 -> parse
- [x] requestLineTokens 객체 만들기
- [x] RequestHandler Parser 스태틱으로 만들기
- [x] RequestHandler.createUser 위치 고민 해보기 -> 이후 생성될 createServlet 내부로 이동예정
- [x] RequestParser에서 String body="" 삭제하기
- [x] RequestHandler에서 메서드 분리하지 말기 (buildRequest, buildResponse)

## Step03 TODO list

- [x] Reqeust 객체 만들기
  - [x] 메서드 종류
  - [x] 리소스 위치
  - [x] 스키마(protocol)
  - [x] Header는 map으로 저장
  - [x] 본문은 String으로

- [x] RequestParser 만들기

- [x] Response 객체 만들기
- [x] response에 값 setting 해주기
- [x] ResponseBuilder 메서드 분리하기
- [x] 회원 저장 하기 (Map형태)
- [x] 중복 회원 방지

## Step01 TODO List

- [x] 기본 소스코드 함께 읽고 분석하기 (요청시 -> mian -> start -> handler.run() 순서로 실행)
- [x] input header를 분석하기 -> requeatLine 분석 -> 데이터 위치(URI) 확인
- [x] 읽은 requestLine를 통해 읽은 자원을 File 객체로 만든다.
- [x] File 객체를 output으로 보낸다.
