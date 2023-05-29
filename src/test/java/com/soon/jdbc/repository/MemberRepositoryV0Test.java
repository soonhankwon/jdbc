package com.soon.jdbc.repository;

import com.soon.jdbc.domain.Member;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

class MemberRepositoryV0Test {

    MemberRepositoryV0 repository = new MemberRepositoryV0();

    @Test
    void crud() throws SQLException {
        Member member  = new Member("memberV2", 10000);
        repository.save(member);

        Member findMember = repository.findById(member.getMemberId());
        System.out.println("findMember = " + findMember);

        assertThat(findMember).isEqualTo(member);
    }
}