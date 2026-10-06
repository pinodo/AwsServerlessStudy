import React, { useState } from "react";
import { signUp, confirmSignUp, signIn, signOut } from "./services/authService";
import { getProfile } from "./services/apiService";

export default function App() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [code, setCode] = useState("");
  const [step, setStep] = useState("LOGIN"); // SIGNUP, CONFIRM, LOGIN, LOGGED_IN
  const [userInfo, setUserInfo] = useState(null);
  const [message, setMessage] = useState("");

  // 회원가입
  const handleSignUp = async () => {
    try {
      await signUp(email, password);
      setMessage("인증 코드가 이메일로 전송되었습니다.");
      setStep("CONFIRM");
    } catch (err) {
      setMessage(`회원가입 실패: ${err.message}`);
    }
  };

  // 이메일 인증
  const handleConfirm = async () => {
    try {
      await confirmSignUp(email, code);
      setMessage("인증 성공! 로그인해주세요.");
      setStep("LOGIN");
    } catch (err) {
      setMessage(`인증 실패: ${err.message}`);
    }
  };

  // 로그인
  const handleSignIn = async () => {
    try {
      await signIn(email, password);
      setMessage("로그인 성공!");
      setStep("LOGGED_IN");
    } catch (err) {
      setMessage(`로그인 실패: ${err.message}`);
    }
  };

  // 백엔드 API 호출 (토큰 검증 테스트)
  const handleFetchProfile = async () => {
    try {
      const data = await getProfile();
      setUserInfo(data);
    } catch (err) {
      setMessage(`API 호출 실패: ${err.message}`);
    }
  };

  // 로그아웃
  const handleSignOut = async () => {
    await signOut();
    setStep("LOGIN");
    setUserInfo(null);
    setMessage("로그아웃 되었습니다.");
  };

  return (
    <div style={{ maxWidth: "400px", margin: "50px auto", fontFamily: "sans-serif", padding: "20px", border: "1px solid #ccc", borderRadius: "8px" }}>
      <h2>Cognito 직접 통신 실습</h2>
      
      {message && <p style={{ color: "blue", fontSize: "14px" }}>{message}</p>}

      {step === "SIGNUP" && (
        <div style={{ display: "flex", flexDirection: "column", gap: "10px" }}>
          <h3>회원가입</h3>
          <input type="email" placeholder="이메일" value={email} onChange={(e) => setEmail(e.target.value)} />
          <input type="password" placeholder="비밀번호" value={password} onChange={(e) => setPassword(e.target.value)} />
          <button onClick={handleSignUp}>가입하기</button>
          <button onClick={() => setStep("LOGIN")}>로그인 화면으로</button>
        </div>
      )}

      {step === "CONFIRM" && (
        <div style={{ display: "flex", flexDirection: "column", gap: "10px" }}>
          <h3>이메일 인증</h3>
          <input type="text" placeholder="6자리 인증코드" value={code} onChange={(e) => setCode(e.target.value)} />
          <button onClick={handleConfirm}>인증 완료</button>
        </div>
      )}

      {step === "LOGIN" && (
        <div style={{ display: "flex", flexDirection: "column", gap: "10px" }}>
          <h3>로그인</h3>
          <input type="email" placeholder="이메일" value={email} onChange={(e) => setEmail(e.target.value)} />
          <input type="password" placeholder="비밀번호" value={password} onChange={(e) => setPassword(e.target.value)} />
          <button onClick={handleSignIn}>로그인</button>
          <button onClick={() => setStep("SIGNUP")}>회원가입 하러가기</button>
        </div>
      )}

      {step === "LOGGED_IN" && (
        <div style={{ display: "flex", flexDirection: "column", gap: "10px" }}>
          <h3>인증된 영역</h3>
          <button onClick={handleFetchProfile}>백엔드 프로필 조회 (Axios)</button>
          <button onClick={handleSignOut} style={{ backgroundColor: "#ff4d4d", color: "white" }}>로그아웃</button>

          {userInfo && (
            <pre style={{ background: "#f4f4f4", padding: "10px", borderRadius: "4px" }}>
              {JSON.stringify(userInfo, null, 2)}
            </pre>
          )}
        </div>
      )}
    </div>
  );
}