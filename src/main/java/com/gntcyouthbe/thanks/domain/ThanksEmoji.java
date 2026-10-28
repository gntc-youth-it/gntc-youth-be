package com.gntcyouthbe.thanks.domain;

// 감사에 누르는 반응. 선언 순서가 FE 크게 보기에 보이는 순서(🙏 ❤️ 👏 🥹 😂)다.
// 운영 DB(PostgreSQL)에는 Hibernate가 이 값들로 check 제약을 만든다. 값을 늘리면 그 제약도 직접 바꿔야 한다.
public enum ThanksEmoji {
    PRAY,
    HEART,
    CLAP,
    TOUCHED,
    LAUGH
}
