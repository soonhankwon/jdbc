package com.soon.jdbc.service;

import com.soon.jdbc.domain.Member;
import com.soon.jdbc.repository.MemberRepositoryV3;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;

/**
 * 트랜잭션 - @Transactional AOP
 */
@Slf4j
public class MemberServiceV3_4 {

    private final MemberRepositoryV3 memberRepository;

    public MemberServiceV3_4(MemberRepositoryV3 memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Transactional
    public void accountTransfer(String fromId, String toId, int money) throws SQLException {
        bizLogic(fromId, toId, money);
    }

    @Transactional
    public void testMethodWithTransactional(String input, Member member) throws SQLException {
        if(input.equals("ex")) {
            log.info("트랜잭션 애노테이션 미적용 코드에서 적용 코드 호출 + exception");
            throw new IllegalStateException();
        } else {
            log.info("트랜잭션 애노테이션 미적용 코드에서 적용 코드 호출 + commit 기대");
            memberRepository.update(member.getMemberId(), 15000);
        }
    }

    public void testMethodWithNoTransactional(Member member) throws SQLException {
        // transaction 미적용 메서드에서 save 로직
        memberRepository.save(member);
        Member findMember = memberRepository.findById(member.getMemberId());
        log.info("트랜잭션 애노테이션 미적용 코드");

        // 1. transaction 적용 메서드 호출 (exception) -> 적용 메서드의 결과는 rollback
//        testMethodWithTransactional("ex", findMember);

        // 2. transaction 적용 메서드 호출 (success)
        testMethodWithTransactional("ok", findMember);

        // 2-1. transaction 적용 메서드 호출 후 exception -> transactional 미적용 메서드 rollback(X)
        if(findMember.getMemberId().equals(member.getMemberId()))
            throw new IllegalStateException();
    }

    private void bizLogic(String fromId, String toId, int money) throws SQLException {
        // business logic
        Member fromMember = memberRepository.findById(fromId);
        Member toMember = memberRepository.findById(toId);

        // get fromId member and transfer money to toId member
        memberRepository.update(fromId, fromMember.getMoney() - money);
        validation(toMember);
        memberRepository.update(toId, toMember.getMoney() + money);
    }

    private void validation(Member toMember) {
        if (toMember.getMemberId().equals("ex")) {
            throw new IllegalStateException("transfer error");
        }
    }
}
