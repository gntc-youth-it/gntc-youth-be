-- Clean up existing data
DELETE FROM advent_verses;
DELETE FROM advent_persons;
DELETE FROM app_user;
DELETE FROM books;

-- Test User
INSERT INTO app_user (id, email, name, provider, provider_user_id, role, created_at, updated_at)
VALUES (1, 'test@example.com', '테스트유저', 'KAKAO', 'kakao_123456', 'USER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Test Book
INSERT INTO books (id, canon_order, book_name, name)
VALUES (1, 1, 'GENESIS', '창세기');

-- Test Verses
INSERT INTO verses (id, book_id, chapter, verse, content, sequence)
VALUES (1, 1, 1, 1, '태초에 하나님이 천지를 창조하시니라', 1);

INSERT INTO verses (id, book_id, chapter, verse, content, sequence)
VALUES (2, 1, 1, 2, '땅이 혼돈하고 공허하며 흑암이 깊음 위에 있고 하나님의 영은 수면 위에 운행하시니라', 2);

-- Test Advent Person and Verses
INSERT INTO advent_persons (id, name, temple, batch)
VALUES (1, '테스트', 'Brisbane', 2024);

INSERT INTO advent_verses (id, person_id, sequence, book_name, chapter, verse)
VALUES (1, 1, 1, 'GENESIS', 1, 1);
