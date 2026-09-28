package com.example.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.example.lambda.dto.CalcRequest;

public class CalculatorHandler implements RequestHandler<CalcRequest, String>{
  @Override
  public String handleRequest(CalcRequest input, Context context) {
    // 1. Logger
    LambdaLogger logger = context.getLogger();

    // 2. Logging input values
    logger.log("Request: " + input);

    // 3. 환경변수 조회
    String calculatorName = System.getenv("CALCULATOR_NAME");
    if (calculatorName == null) {
      calculatorName = "Lambda Calculator"; // default
    }

    try {
      // 4. Parsing input values
      double num1 = Double.parseDouble(input.num1());
      double num2 = Double.parseDouble(input.num2());
      String op = input.op();

      // 5. 연산
      double result = 0;
      switch (op) {
        case "+": result = num1 + num2; break;
        case "-": result = num1 + num2; break;
        case "*": result = num1 + num2; break;
        case "/":
          if (num2 == 0)
            throw new IllegalArgumentException("0으로 나눌 수 없습니다.");
          result = num1 / num2;
        default:
          logger.log("Invalid Operation: " + input.op());
          throw new IllegalArgumentException(input.op() + "는 지원하지 않는 연산자입니다.");
      }

      // 6. Return result
      return String.format("[%s] 결과: %.1f %s %.1f = %.2f", calculatorName, num1, op, num2, result);
    } catch (Exception e) {
      logger.log("Error: " + e.getMessage());
      return "[Error] 계산 중 오류가 발생했습니다: " + e.getMessage();
    }
  }
}
