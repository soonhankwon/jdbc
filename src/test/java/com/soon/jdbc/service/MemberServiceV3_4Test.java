package com.soon.jdbc.service;

import com.soon.jdbc.domain.Member;
import com.soon.jdbc.repository.MemberRepositoryV3;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.sql.SQLException;

import static com.soon.jdbc.connection.ConnectionConst.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class MemberServiceV3_4Test {

    public static final String MEMBER_A = "memberA";
    public static final String MEMBER_B = "memberB";
    public static final String MEMBER_EX = "ex";

    @Autowired
    private MemberRepositoryV3 memberRepository;
    @Autowired
    private MemberServiceV3_4 memberService;

    @TestConfiguration
    static class TestConfig {
        @Bean
        DataSource dataSource() {
            return new DriverManagerDataSource(URL, USERNAME, PASSWORD);
        }

        @Bean
        PlatformTransactionManager transactionManager() {
            return new DataSourceTransactionManager(dataSource());
        }

        @Bean
        MemberRepositoryV3 memberRepositoryV3() {
            return new MemberRepositoryV3(dataSource());
        }

        @Bean
        MemberServiceV3_4 memberServiceV3_4() {
            return new MemberServiceV3_4(memberRepositoryV3());
        }

    }

    @AfterEach
    void after() throws SQLException {
        memberRepository.delete(MEMBER_A);
        memberRepository.delete(MEMBER_B);
        memberRepository.delete(MEMBER_EX);
    }

    @Test
    @DisplayName("트랜잭션 미적용 메서드에서 트랜잭션 적용 메서드 호출 테스트")
    void test() throws SQLException {
        System.out.println("memberService class= " + memberService.getClass());
        // transaction proxy
        System.out.println("memberRepository class= " + memberRepository.getClass());
        Member memberA = new Member(MEMBER_A, 10000);

        // memberA 를 저장할시 트랜잭션 미적용 메서드에서 exception 발생
        assertThatThrownBy(() ->memberService.testMethodWithNoTransactional(memberA))
                .isInstanceOf(IllegalStateException.class);

        // 트랜잭션 미적용 메서드에선 rollback 이 안된다.
        Member findMemberA = memberRepository.findById(memberA.getMemberId());
        assertThat(findMemberA.getMoney()).isEqualTo(15000);
    }
}