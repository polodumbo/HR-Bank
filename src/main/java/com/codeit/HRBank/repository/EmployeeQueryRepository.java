package com.codeit.HRBank.repository;

import com.codeit.HRBank.domain.Employee;
import com.codeit.HRBank.domain.QChange_log;
import com.codeit.HRBank.domain.QEmployee;
import com.codeit.HRBank.dto.data.EmployeeTrendDto;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.DatePath;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.core.types.dsl.StringTemplate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class EmployeeQueryRepository {
    private final JPAQueryFactory queryFactory; //Querydsl쿼리를 생성하고 실행하는 핵심객체
    private static final QEmployee employee = QEmployee.employee; // <-- QEmployee 인스턴스 사용 employees 테이블의 각 컬럼에 접근가능
    private static final QChange_log changeLog = QChange_log.change_log; // <-- QEmployee 인스턴스 사용 employees 테이블의 각 컬럼에 접근가능

    public List<Tuple> getEmployeeTrend(LocalDate from, LocalDate to, String unit) {
        //format : SQL함수 템플릿을 만듦
        //DATE_TRUNC : PostgreSQL에 사용되는 함수로, 날짜/시간 값을 특정단위(month, year등)의 시작시점으로 잘라내는 역할
        String dateTruncFormat = String.format("DATE_TRUNC('%s', {0})", unit);

        // QEmployee의 hireDate 필드를 사용
        DatePath<LocalDate> hireDatePath = employee.hireDate;
        //hireDate 를 DATE_TRUNC 를 사용해 날짜 단위로 쪼개라는 SQL표현식을 만듦
        StringTemplate hireDateExpression = Expressions.stringTemplate(
                dateTruncFormat,
                hireDatePath // <-- QEmployee 필드 전달
        );

        //Querydsl쿼리
        List<Tuple> results = queryFactory
                .select(
                        hireDateExpression.as("date"),  //쪼갠날짜단위
                        employee.id.countDistinct().as("count") //COUNT(id)
                )
                .from(employee)
                .where(hireDatePath.between(from, to))  //기간설정
                .groupBy(hireDateExpression)        //기간으로 그룹화
                .having()
                .orderBy(hireDateExpression.asc())  //
                .fetch();

        return results;
    }
}
