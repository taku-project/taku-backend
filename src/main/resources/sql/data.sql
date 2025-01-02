INSERT INTO users (
    age_range, created_at, updated_at, role, status, gender, provider_type, domestic_id, nickname, profile_img , email
) VALUES (
    NULL, '2024-12-15T19:37:15.077585', '2024-12-15T19:37:15.077585', 'USER', 'ACTIVE', 'MEN', 'KAKAO', 'ses9892@naver.com', 'looco1', 'https://duckwho-video.ce400acf0a8ec87265c8fda6ec68f959.r2.cloudflarestorage.com/a6d900d2-eac5-49e1-813e-f1cd36f8095b.jpg', 'ses9892@naver.com'
);

INSERT INTO ani_genres (id, genre_name, created_at, updated_at)
VALUES
(1, '액션', NOW(), NOW()),
(2, '모험', NOW(), NOW()),
(3, '판타지', NOW(), NOW()),
(4, '드라마', NOW(), NOW()),
(5, '코미디', NOW(), NOW());

INSERT INTO item_categories (item_category_id, name, created_at, updated_at)
VALUES
    (1, '만화책', NOW(), NOW()),
    (2, '피규어', NOW(), NOW()),
    (3, '코스프레 의상', NOW(), NOW()),
    (4, '전자제품', NOW(), NOW()),
    (5, '굿즈', NOW(), NOW()),
    (6, '문구', NOW(), NOW()),
    (7, '기타', NOW(), NOW());