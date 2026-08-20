-- Clean up existing data
DELETE FROM video;
DELETE FROM post_image;
DELETE FROM post_hashtag;
DELETE FROM post_church;
DELETE FROM post_like;
DELETE FROM post_comment;
DELETE FROM post;
DELETE FROM uploaded_file;
DELETE FROM user_profile;
DELETE FROM advent_verses;
DELETE FROM advent_persons;
DELETE FROM app_user;
DELETE FROM church;
DELETE FROM verses;
DELETE FROM books;

-- Test Church
INSERT INTO church (id, created_at, updated_at)
VALUES ('ANYANG', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO church (id, created_at, updated_at)
VALUES ('SUWON', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Test User
INSERT INTO app_user (id, email, name, provider, provider_user_id, role, created_at, updated_at)
VALUES (1, 'test@example.com', '테스트유저', 'KAKAO', 'kakao_123456', 'USER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO app_user (id, email, name, provider, provider_user_id, role, church_id, created_at, updated_at)
VALUES (2, 'leader@example.com', '리더유저', 'KAKAO', 'kakao_789012', 'LEADER', 'ANYANG', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO app_user (id, email, name, provider, provider_user_id, role, church_id, created_at, updated_at)
VALUES (3, 'master@example.com', '마스터유저', 'KAKAO', 'kakao_345678', 'MASTER', 'ANYANG', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 수원 성전 소속 일반 사용자 (역할 변경 테스트용)
INSERT INTO app_user (id, email, name, provider, provider_user_id, role, church_id, created_at, updated_at)
VALUES (4, 'suwon-user@example.com', '수원유저', 'KAKAO', 'kakao_111222', 'USER', 'SUWON', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 안양 성전 소속 일반 사용자 (역할 변경 테스트용 - 기존 리더 강등 확인)
INSERT INTO app_user (id, email, name, provider, provider_user_id, role, church_id, created_at, updated_at)
VALUES (5, 'anyang-user@example.com', '안양유저', 'KAKAO', 'kakao_333444', 'USER', 'ANYANG', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 안양 성전 소속 담당자 (MANAGER 권한 테스트용)
INSERT INTO app_user (id, email, name, provider, provider_user_id, role, church_id, created_at, updated_at)
VALUES (6, 'manager@example.com', '담당자유저', 'KAKAO', 'kakao_555666', 'MANAGER', 'ANYANG', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Profile Image for master user
INSERT INTO uploaded_file (id, original_filename, stored_filename, file_path, content_type, file_size, created_at, updated_at)
VALUES (800, 'profile.jpg', 'stored_profile.jpg', 'uploads/profile.jpg', 'image/jpeg', 512, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Test UserProfile (리더유저에게 프로필 추가)
INSERT INTO user_profile (id, user_id, generation, phone_number, gender, created_at, updated_at)
VALUES (100, 2, 45, '010-1234-5678', 'MALE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Test UserProfile (마스터유저에게 프로필 + 프로필 이미지 추가)
INSERT INTO user_profile (id, user_id, generation, phone_number, gender, profile_image_id, created_at, updated_at)
VALUES (101, 3, 44, '010-9876-5432', 'MALE', 800, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
ALTER TABLE user_profile ALTER COLUMN id RESTART WITH 102;

-- Test Book
INSERT INTO books (id, canon_order, book_name, name)
VALUES (1, 1, 'GENESIS', '창세기');

INSERT INTO books (id, canon_order, book_name, name)
VALUES (6, 6, 'JOSHUA', '여호수아');

INSERT INTO books (id, canon_order, book_name, name)
VALUES (23, 23, 'ISAIAH', '이사야');

-- Test Verse (이사야 40:31)
INSERT INTO verses (id, book_id, chapter, verse, content, sequence)
VALUES (1, 23, 40, 31, '오직 여호와를 앙망하는 자는 새 힘을 얻으리니 독수리의 날개치며 올라감 같을 것이요 달음박질하여도 곤비치 아니하겠고 걸어가도 피곤치 아니하리로다', 23662);

-- Test Verse (여호수아 1:7)
INSERT INTO verses (id, book_id, chapter, verse, content, sequence)
VALUES (2, 6, 1, 7, '오직 너는 마음을 강하게 하고 극히 담대히 하여 나의 종 모세가 네게 명한 율법을 다 지켜 행하고 좌로나 우로나 치우치지 말라 그리하면 어디로 가든지 형통하리니', 5859);

-- Test Advent Person and Verses
INSERT INTO advent_persons (id, name, temple, batch)
VALUES (1, '테스트', 'Brisbane', 2024);

INSERT INTO advent_verses (id, person_id, sequence, book_name, chapter, verse)
VALUES (1, 1, 1, 'GENESIS', 1, 1);

-- Gallery Test: Uploaded Files
INSERT INTO uploaded_file (id, original_filename, stored_filename, file_path, content_type, file_size, created_at, updated_at)
VALUES (901, 'gallery1.jpg', 'stored_gallery1.jpg', 'uploads/gallery1.jpg', 'image/jpeg', 1024, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO uploaded_file (id, original_filename, stored_filename, file_path, content_type, file_size, created_at, updated_at)
VALUES (902, 'gallery2.jpg', 'stored_gallery2.jpg', 'uploads/gallery2.jpg', 'image/jpeg', 1024, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO uploaded_file (id, original_filename, stored_filename, file_path, content_type, file_size, created_at, updated_at)
VALUES (903, 'gallery3.jpg', 'stored_gallery3.jpg', 'uploads/gallery3.jpg', 'image/jpeg', 1024, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO uploaded_file (id, original_filename, stored_filename, file_path, content_type, file_size, created_at, updated_at)
VALUES (904, 'gallery4.jpg', 'stored_gallery4.jpg', 'uploads/gallery4.jpg', 'image/jpeg', 1024, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Gallery Test: APPROVED post (RETREAT_2026_WINTER, by master user 3)
INSERT INTO post (id, author_id, sub_category, status, content, is_author_public, created_at, updated_at, created_by, updated_by)
VALUES (901, 3, 'RETREAT_2026_WINTER', 'APPROVED', '승인된 수련회 게시글', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 3, 3);

-- Gallery Test: APPROVED post (NONE, by master user 3)
INSERT INTO post (id, author_id, sub_category, status, content, is_author_public, created_at, updated_at, created_by, updated_by)
VALUES (902, 3, 'NONE', 'APPROVED', '승인된 기타 게시글', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 3, 3);

-- Gallery Test: PENDING_REVIEW post (RETREAT_2026_WINTER, by regular user 1)
INSERT INTO post (id, author_id, sub_category, status, content, is_author_public, created_at, updated_at, created_by, updated_by)
VALUES (903, 1, 'RETREAT_2026_WINTER', 'PENDING_REVIEW', '검수대기 게시글', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1);

-- Gallery Test: Images for APPROVED post 901 (RETREAT_2026_WINTER)
INSERT INTO post_image (id, post_id, uploaded_file_id, sort_order, created_at, updated_at, created_by, updated_by)
VALUES (901, 901, 901, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 3, 3);

INSERT INTO post_image (id, post_id, uploaded_file_id, sort_order, created_at, updated_at, created_by, updated_by)
VALUES (902, 901, 902, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 3, 3);

-- Gallery Test: Image for APPROVED post 902 (NONE)
INSERT INTO post_image (id, post_id, uploaded_file_id, sort_order, created_at, updated_at, created_by, updated_by)
VALUES (903, 902, 903, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 3, 3);

-- Gallery Test: Image for PENDING_REVIEW post 903 (should NOT appear in gallery)
INSERT INTO post_image (id, post_id, uploaded_file_id, sort_order, created_at, updated_at, created_by, updated_by)
VALUES (904, 903, 904, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1);

-- Church Tag: post 901 -> ANYANG, post 902 -> SUWON
INSERT INTO post_church (post_id, church_id) VALUES (901, 'ANYANG');
INSERT INTO post_church (post_id, church_id) VALUES (902, 'SUWON');

-- Delete Test: Uploaded Files for post 904
INSERT INTO uploaded_file (id, original_filename, stored_filename, file_path, content_type, file_size, created_at, updated_at)
VALUES (905, 'delete1.jpg', 'stored_delete1.jpg', 'uploads/stored_delete1.jpg', 'image/jpeg', 1024, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO uploaded_file (id, original_filename, stored_filename, file_path, content_type, file_size, created_at, updated_at)
VALUES (906, 'delete2.jpg', 'stored_delete2.jpg', 'uploads/stored_delete2.jpg', 'image/jpeg', 1024, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Delete Test: APPROVED post for deletion testing (NONE category, no church tag)
INSERT INTO post (id, author_id, sub_category, status, content, is_author_public, created_at, updated_at, created_by, updated_by)
VALUES (904, 3, 'NONE', 'APPROVED', '삭제 테스트용 게시글', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 3, 3);

INSERT INTO post_image (id, post_id, uploaded_file_id, sort_order, created_at, updated_at, created_by, updated_by)
VALUES (905, 904, 905, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 3, 3);

INSERT INTO post_image (id, post_id, uploaded_file_id, sort_order, created_at, updated_at, created_by, updated_by)
VALUES (906, 904, 906, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 3, 3);

INSERT INTO post_comment (id, post_id, author_id, content, created_at, updated_at, created_by, updated_by)
VALUES (901, 904, 1, '삭제 테스트 댓글', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1);

INSERT INTO post_like (id, post_id, user_id, created_at, updated_at, created_by, updated_by)
VALUES (901, 904, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1);

-- Approve Test: PENDING_REVIEW post for approval testing
INSERT INTO post (id, author_id, sub_category, status, content, is_author_public, created_at, updated_at, created_by, updated_by)
VALUES (905, 1, 'RETREAT_2026_WINTER', 'PENDING_REVIEW', '승인 테스트용 검수대기 게시글', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1);

-- Random Photos Test: Additional uploaded files for ANYANG church
INSERT INTO uploaded_file (id, original_filename, stored_filename, file_path, content_type, file_size, created_at, updated_at)
VALUES (907, 'random1.jpg', 'stored_random1.jpg', 'uploads/random1.jpg', 'image/jpeg', 1024, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO uploaded_file (id, original_filename, stored_filename, file_path, content_type, file_size, created_at, updated_at)
VALUES (908, 'random2.jpg', 'stored_random2.jpg', 'uploads/random2.jpg', 'image/jpeg', 1024, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO uploaded_file (id, original_filename, stored_filename, file_path, content_type, file_size, created_at, updated_at)
VALUES (909, 'random3.jpg', 'stored_random3.jpg', 'uploads/random3.jpg', 'image/jpeg', 1024, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO uploaded_file (id, original_filename, stored_filename, file_path, content_type, file_size, created_at, updated_at)
VALUES (910, 'random4.jpg', 'stored_random4.jpg', 'uploads/random4.jpg', 'image/jpeg', 1024, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO uploaded_file (id, original_filename, stored_filename, file_path, content_type, file_size, created_at, updated_at)
VALUES (911, 'random5.jpg', 'stored_random5.jpg', 'uploads/random5.jpg', 'image/jpeg', 1024, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO uploaded_file (id, original_filename, stored_filename, file_path, content_type, file_size, created_at, updated_at)
VALUES (912, 'random6.jpg', 'stored_random6.jpg', 'uploads/random6.jpg', 'image/jpeg', 1024, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Random Photos Test: APPROVED post for ANYANG with 6 images
INSERT INTO post (id, author_id, sub_category, status, content, is_author_public, created_at, updated_at, created_by, updated_by)
VALUES (906, 3, 'NONE', 'APPROVED', '안양 랜덤 사진 테스트 게시글', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 3, 3);

INSERT INTO post_church (post_id, church_id) VALUES (906, 'ANYANG');

INSERT INTO post_image (id, post_id, uploaded_file_id, sort_order, created_at, updated_at, created_by, updated_by)
VALUES (907, 906, 907, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 3, 3);
INSERT INTO post_image (id, post_id, uploaded_file_id, sort_order, created_at, updated_at, created_by, updated_by)
VALUES (908, 906, 908, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 3, 3);
INSERT INTO post_image (id, post_id, uploaded_file_id, sort_order, created_at, updated_at, created_by, updated_by)
VALUES (909, 906, 909, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 3, 3);
INSERT INTO post_image (id, post_id, uploaded_file_id, sort_order, created_at, updated_at, created_by, updated_by)
VALUES (910, 906, 910, 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 3, 3);
INSERT INTO post_image (id, post_id, uploaded_file_id, sort_order, created_at, updated_at, created_by, updated_by)
VALUES (911, 906, 911, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 3, 3);
INSERT INTO post_image (id, post_id, uploaded_file_id, sort_order, created_at, updated_at, created_by, updated_by)
VALUES (912, 906, 912, 6, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 3, 3);

-- Video Test: 삭제 테스트용 영상
INSERT INTO video (id, title, link, sub_category, created_at, updated_at, created_by, updated_by)
VALUES (1001, '수련회 찬양 모음', 'https://www.youtube.com/watch?v=video1', 'RETREAT_2026_WINTER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 3, 3);

-- Video Test: 삭제 권한 테스트용 영상
INSERT INTO video (id, title, link, sub_category, created_at, updated_at, created_by, updated_by)
VALUES (1002, '수련회 설교 영상', 'https://www.youtube.com/watch?v=video2', 'RETREAT_2026_WINTER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 3, 3);

-- Video Test: 기타 카테고리 영상 (필터링 테스트용)
INSERT INTO video (id, title, link, sub_category, created_at, updated_at, created_by, updated_by)
VALUES (1003, '기타 영상', 'https://www.youtube.com/watch?v=video3', 'NONE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 3, 3);

-- 여름 수련회 Test: 프로그램별 게시글 이미지
INSERT INTO uploaded_file (id, original_filename, stored_filename, file_path, content_type, file_size, created_at, updated_at)
VALUES (913, 'summer-sports.jpg', 'stored_summer-sports.jpg', 'uploads/summer-sports.jpg', 'image/jpeg', 1024, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO uploaded_file (id, original_filename, stored_filename, file_path, content_type, file_size, created_at, updated_at)
VALUES (914, 'summer-walk.jpg', 'stored_summer-walk.jpg', 'uploads/summer-walk.jpg', 'image/jpeg', 1024, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO uploaded_file (id, original_filename, stored_filename, file_path, content_type, file_size, created_at, updated_at)
VALUES (915, 'summer-etc.jpg', 'stored_summer-etc.jpg', 'uploads/summer-etc.jpg', 'image/jpeg', 1024, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 여름 수련회 Test: 체육대회 게시글 (APPROVED)
INSERT INTO post (id, author_id, sub_category, status, content, is_author_public, created_at, updated_at, created_by, updated_by)
VALUES (907, 3, 'RETREAT_2026_SUMMER_SPORTS', 'APPROVED', '체육대회 게시글', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 3, 3);

-- 여름 수련회 Test: 함께걷장 게시글 (APPROVED)
INSERT INTO post (id, author_id, sub_category, status, content, is_author_public, created_at, updated_at, created_by, updated_by)
VALUES (908, 3, 'RETREAT_2026_SUMMER_WALK', 'APPROVED', '함께걷장 게시글', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 3, 3);

-- 여름 수련회 Test: 그외 활동 게시글 (APPROVED)
INSERT INTO post (id, author_id, sub_category, status, content, is_author_public, created_at, updated_at, created_by, updated_by)
VALUES (909, 3, 'RETREAT_2026_SUMMER_ETC', 'APPROVED', '그외 활동 게시글', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 3, 3);

INSERT INTO post_image (id, post_id, uploaded_file_id, sort_order, created_at, updated_at, created_by, updated_by)
VALUES (913, 907, 913, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 3, 3);

INSERT INTO post_image (id, post_id, uploaded_file_id, sort_order, created_at, updated_at, created_by, updated_by)
VALUES (914, 908, 914, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 3, 3);

INSERT INTO post_image (id, post_id, uploaded_file_id, sort_order, created_at, updated_at, created_by, updated_by)
VALUES (915, 909, 915, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 3, 3);

-- 여름 수련회 Test: 프로그램별 영상
INSERT INTO video (id, title, link, sub_category, created_at, updated_at, created_by, updated_by)
VALUES (1004, '체육대회 하이라이트', 'https://www.youtube.com/watch?v=video4', 'RETREAT_2026_SUMMER_SPORTS', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 3, 3);
