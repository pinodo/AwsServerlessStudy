import {
  SignUpCommand,
  ConfirmSignUpCommand,
  InitiateAuthCommand,
  GlobalSignOutCommand
} from "@aws-sdk/client-cognito-identity-provider";
import { cognitoClient } from "../config/cognitoClient";

const CLIENT_ID = import.meta.env.VITE_COGNITO_CLIENT_ID;

// 1. 회원가입
export const signUp = async (email, password) => {
  const command = new SignUpCommand({
    ClientId: CLIENT_ID,
    Username: email,
    Password: password,
    UserAttributes: [{ Name: "email", Value: email }]
  });
  return await cognitoClient.send(command);
};

// 2. 이메일 인증코드 확인
export const confirmSignUp = async (email, code) => {
  const command = new ConfirmSignUpCommand({
    ClientId: CLIENT_ID,
    Username: email,
    ConfirmationCode: code
  });
  return await cognitoClient.send(command);
};

// 3. 로그인 및 sessionStorage에 토큰 저장
export const signIn = async (email, password) => {
  const command = new InitiateAuthCommand({
    AuthFlow: "USER_PASSWORD_AUTH",
    ClientId: CLIENT_ID,
    AuthParameters: {
      USERNAME: email,
      PASSWORD: password
    }
  });

  const response = await cognitoClient.send(command);
  const authResult = response.AuthenticationResult;

  if (authResult) {
    // sessionStorage에 토큰 보관
    sessionStorage.setItem("accessToken", authResult.AccessToken);
    sessionStorage.setItem("idToken", authResult.IdToken);
    sessionStorage.setItem("refreshToken", authResult.RefreshToken);
  }
  return authResult;
};

// 4. 로그아웃
export const signOut = async () => {
  const accessToken = sessionStorage.getItem("accessToken");
  if (accessToken) {
    try {
      const command = new GlobalSignOutCommand({
        AccessToken: accessToken
      });
      await cognitoClient.send(command);
    } catch (e) {
      console.error("SignOut Error:", e);
    }
  }
  sessionStorage.clear();
};