package com.codeit.HRBank.repository;

import com.codeit.HRBank.domain.EmploymentStatus;
import com.codeit.HRBank.domain.QChangeLog;
import com.codeit.HRBank.domain.QChangeLogDiff;
import com.codeit.HRBank.domain.QDepartment;
import com.codeit.HRBank.domain.QEmployee;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.DatePath;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.core.types.dsl.StringTemplate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class EmployeeQueryRepository {

    private final JPAQueryFactory queryFactory; //Querydsl쿼리를 생성하고 실행하는 핵심객체
    private static final QEmployee employee = QEmployee.employee; // <-- QEmployee 인스턴스 사용 employees 테이블의 각 컬럼에 접근가능
    private static final QChangeLog changeLog = QChangeLog.changeLog;
    private static final QChangeLogDiff changeLogDiff = QChangeLogDiff.changeLogDiff;
    private static final QDepartment department = QDepartment.department;


    public List<Tuple> getDistributionTuple(String groupBy, EmploymentStatus status) {
        StringExpression groupByKey;
        if ("department".equalsIgnoreCase(groupBy)) {
            groupByKey = department.name;
        } else if ("position".equalsIgnoreCase(groupBy)) {
            groupByKey = employee.position;
        } else {
            groupByKey = department.name; // 기본값
        }

        return queryFactory
            .select(
                groupByKey,
                employee.id.countDistinct()
            )
            .from(employee)
            .leftJoin(employee.department, department)
            .where(employee.status.eq(status))
            .groupBy(groupByKey)
            .orderBy(employee.id.countDistinct().desc())
            .fetch();
    }

    // 기간 시작 전 총 직원 수 조회
    public long getEmployeeCountBefore(LocalDate date) {
        // 지정된 날짜 이전에 입사한 직원 수
        long hiredBefore = queryFactory
            .select(employee.id.countDistinct())
            .from(employee)
            .where(employee.hireDate.lt(date))
            .fetchOne();

        // 지정된 날짜 이전에 퇴사한 직원 수
        long resignedBefore = queryFactory
            .select(changeLog.id.countDistinct())
            .from(changeLogDiff)
            .innerJoin(changeLogDiff.log, changeLog)
            .where(
                changeLog.at.lt(date.atStartOfDay()),
                changeLogDiff.propertyName.eq("status"),
                changeLogDiff.afterValue.eq("RESIGNED")
            )
            .fetchOne();

        return hiredBefore - resignedBefore;
    }

    // 전체 직원 수를 세는 쿼리 메서드 (레포지토리에도 추가해야 함)
    // 이 메서드는 EmployeeQueryRepository에 추가되어야 합니다.
    public Long countByStatus(EmploymentStatus status) {
        return queryFactory
            .select(employee.id.countDistinct())
            .from(employee)
            .where(employee.status.eq(status))
            .fetchOne();
    }

    public List<Tuple> getHiredTrend(LocalDate from, LocalDate to, String unit) {
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
            .orderBy(hireDateExpression.asc())  //
            .fetch();

        return results;
    }

    public List<Tuple> getResignedTrend(LocalDate from, LocalDate to, String unit) {
        //format : SQL함수 템플릿을 만듦
        //DATE_TRUNC : PostgreSQL에 사용되는 함수로, 날짜/시간 값을 특정단위(month, year등)의 시작시점으로 잘라내는 역할
        String dateTruncFormat = String.format("DATE_TRUNC('%s', {0})", unit);

        // QChangLog at 필드를 사용
        DatePath<LocalDateTime> resignedDatePath = Expressions.datePath(
            LocalDateTime.class,
            changeLog.at.getMetadata().getName()
        );
        //hireDate 를 DATE_TRUNC 를 사용해 날짜 단위로 쪼개라는 SQL표현식을 만듦
        StringTemplate resignedDateExpression = Expressions.stringTemplate(
            dateTruncFormat,
            resignedDatePath // <-- QEmployee 필드 전달
        );

        return queryFactory
            .select(
                resignedDateExpression.as("date"),  //쪼갠날짜단위
                changeLog.id.countDistinct().as("count") //COUNT(id)
            )
            .from(changeLogDiff)
            .innerJoin(changeLogDiff.log, changeLog) // <-- diffs.log를 통해 changeLog와 조인
            .where(
                resignedDatePath.between(from.atStartOfDay(), to.atTime(23, 59, 59)),
//                        changeLog.at.between(from.atStartOfDay(), to.atTime(23, 59, 59)),
                changeLogDiff.propertyName.eq("status"),
                changeLogDiff.afterValue.eq("RESIGNED")
            )  //기간설정
            .groupBy(resignedDateExpression)        //기간으로 그룹화
            .orderBy(resignedDateExpression.asc())  //
            .fetch();
    }

}
