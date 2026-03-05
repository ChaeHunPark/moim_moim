-- 1. 먼저 기존에 null이었던 데이터를 기본값으로 채워줍니다.
UPDATE participation
SET join_reason = '기존 데이터 자동 생성'
WHERE join_reason IS NULL;

-- 2. (선택사항) 앞으로 null이 들어오지 못하도록 제약 조건을 강화
-- 이미 엔티티에서 설정했지만, DB 레벨에서도 한 번 더 잠그기.
ALTER TABLE participation MODIFY COLUMN join_reason VARCHAR(255) NOT NULL;