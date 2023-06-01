package com.soon.jdbc.exception.translator;

import com.soon.jdbc.connection.ConnectionConst;
import com.soon.jdbc.connection.DBConnectionUtil;
import com.soon.jdbc.domain.Member;
import com.soon.jdbc.repository.ex.MyDbException;
import com.soon.jdbc.repository.ex.MyDuplicateKeyException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.support.JdbcUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Random;

import static com.soon.jdbc.connection.ConnectionConst.*;

public class ExTranslatorV1Test {

    Repository repository;
    Service service;

    @BeforeEach
    void init() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
        repository = new Repository(dataSource);
        service = new Service(repository);
    }

    @Test
    void duplicatedKeySave() {
        service.create("myID");
        service.create("myID");
    }

    static class Service {
        private final Repository repository;

        public Service(Repository repository) {
            this.repository = repository;
        }

        public void create(String memberId) {
            try {
                repository.save(new Member(memberId, 0));
                System.out.println("memberId = " + memberId);
            } catch (MyDuplicateKeyException e) {
                System.out.println("duplicated key, try restore");
                String retryId = generateNewId(memberId);
                System.out.println("retryId = " + retryId);
                repository.save(new Member(retryId, 0));
            } catch (MyDbException e) {
                System.out.println("데이터 접근 계층 예외" + e);
                throw e;
            }
        }

        private String generateNewId(String memberId) {
            return memberId + new Random().nextInt(10000);
        }
    }


    static class Repository {
        private final DataSource dataSource;

        public Repository(DataSource dataSource) {
            this.dataSource = dataSource;
        }

        public Member save(Member member) {
            String sql = "insert into member(member_id, money) values (?,?)";
            Connection con = null;
            PreparedStatement pstmt = null;

            try {
                con = dataSource.getConnection();
                pstmt = con.prepareStatement(sql);
                pstmt.setString(1, member.getMemberId());
                pstmt.setInt(2, member.getMoney());
                pstmt.executeUpdate();
                return member;
            } catch (SQLException e) {
                if (e.getErrorCode() == 23505) {
                    throw new MyDuplicateKeyException(e);
                }
                throw new MyDbException(e);
            } finally {
                JdbcUtils.closeStatement(pstmt);
                JdbcUtils.closeConnection(con);
            }
        }
    }
}
