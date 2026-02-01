#!/usr/bin/env python3
"""
어드벤트 캘린더 데이터 적재 스크립트

사용법:
1. 구글 스프레드시트에서 CSV로 다운로드 (이름, 성전, 기수, 내용 컬럼)
2. .env 파일에 환경변수 설정
3. python advent_loader.py input.csv

필요한 패키지:
pip install openai psycopg2-binary python-dotenv pandas
"""

import os
import sys
import json
import pandas as pd
import psycopg2
from openai import OpenAI
from dotenv import load_dotenv

load_dotenv()

# 환경변수
OPENAI_API_KEY = os.getenv("OPENAI_API_KEY")
DB_HOST = os.getenv("DB_HOST", "localhost")
DB_PORT = os.getenv("DB_PORT", "5432")
DB_NAME = os.getenv("DB_NAME")
DB_USER = os.getenv("DB_USER")
DB_PASSWORD = os.getenv("DB_PASSWORD")

# 성경 책 목록 (BookName Enum과 동일)
BOOK_NAMES = [
    "GENESIS", "EXODUS", "LEVITICUS", "NUMBERS", "DEUTERONOMY",
    "JOSHUA", "JUDGES", "RUTH", "FIRST_SAMUEL", "SECOND_SAMUEL",
    "FIRST_KINGS", "SECOND_KINGS", "FIRST_CHRONICLES", "SECOND_CHRONICLES",
    "EZRA", "NEHEMIAH", "ESTHER", "JOB", "PSALMS", "PROVERBS",
    "ECCLESIASTES", "SONG_OF_SONGS", "ISAIAH", "JEREMIAH", "LAMENTATIONS",
    "EZEKIEL", "DANIEL", "HOSEA", "JOEL", "AMOS", "OBADIAH", "JONAH",
    "MICAH", "NAHUM", "HABAKKUK", "ZEPHANIAH", "HAGGAI", "ZECHARIAH",
    "MALACHI", "MATTHEW", "MARK", "LUKE", "JOHN", "ACTS", "ROMANS",
    "FIRST_CORINTHIANS", "SECOND_CORINTHIANS", "GALATIANS", "EPHESIANS",
    "PHILIPPIANS", "COLOSSIANS", "FIRST_THESSALONIANS", "SECOND_THESSALONIANS",
    "FIRST_TIMOTHY", "SECOND_TIMOTHY", "TITUS", "PHILEMON", "HEBREWS",
    "JAMES", "FIRST_PETER", "SECOND_PETER", "FIRST_JOHN", "SECOND_JOHN",
    "THIRD_JOHN", "JUDE", "REVELATION"
]


def get_ai_recommendations(client: OpenAI, resolution: str) -> list[dict]:
    """AI를 사용해 다짐 기반 28개 말씀 추천"""

    prompt = f"""당신은 성경 말씀 추천 전문가입니다.
사용자의 다짐/결심을 읽고, 그에 맞는 성경 구절 28개를 추천해주세요.

사용자의 다짐:
"{resolution}"

요구사항:
1. 정확히 28개의 구절을 추천해주세요 (어드벤트 캘린더용)
2. 다짐의 내용과 연관된 말씀을 선택해주세요
3. 구약과 신약을 적절히 섞어주세요
4. 실제로 존재하는 구절만 추천해주세요

응답 형식 (JSON 배열로만 응답):
[
  {{"book_name": "GENESIS", "chapter": 1, "verse": 1}},
  {{"book_name": "PSALMS", "chapter": 23, "verse": 1}},
  ...
]

book_name은 반드시 다음 중 하나여야 합니다:
{', '.join(BOOK_NAMES)}

JSON 배열만 응답하세요. 다른 텍스트는 포함하지 마세요."""

    response = client.chat.completions.create(
        model="gpt-4o",
        messages=[{"role": "user", "content": prompt}],
        max_tokens=2000,
        temperature=0.7
    )

    response_text = response.choices[0].message.content.strip()

    # JSON 파싱
    try:
        verses = json.loads(response_text)
    except json.JSONDecodeError:
        # JSON 부분만 추출 시도
        import re
        json_match = re.search(r'\[.*\]', response_text, re.DOTALL)
        if json_match:
            verses = json.loads(json_match.group())
        else:
            raise ValueError(f"Failed to parse AI response: {response_text}")

    # 검증
    if len(verses) != 28:
        print(f"Warning: Got {len(verses)} verses instead of 28")

    for v in verses:
        if v["book_name"] not in BOOK_NAMES:
            raise ValueError(f"Invalid book name: {v['book_name']}")

    return verses[:28]  # 28개만 반환


def insert_to_db(conn, name: str, temple: str, batch: int, verses: list[dict]):
    """DB에 데이터 적재"""

    with conn.cursor() as cur:
        # 1. AdventPerson 삽입
        cur.execute("""
            INSERT INTO advent_persons (name, temple, batch)
            VALUES (%s, %s, %s)
            ON CONFLICT (name, temple, batch) DO UPDATE SET name = EXCLUDED.name
            RETURNING id
        """, (name, temple, batch))
        person_id = cur.fetchone()[0]

        # 2. 기존 verses 삭제 (재실행 대비)
        cur.execute("DELETE FROM advent_verses WHERE person_id = %s", (person_id,))

        # 3. AdventVerse 삽입
        for idx, v in enumerate(verses, start=1):
            cur.execute("""
                INSERT INTO advent_verses (person_id, sequence, book_name, chapter, verse)
                VALUES (%s, %s, %s, %s, %s)
            """, (person_id, idx, v["book_name"], v["chapter"], v["verse"]))

        conn.commit()
        print(f"  ✓ Inserted {len(verses)} verses for {name}")


def main():
    if len(sys.argv) < 2:
        print("Usage: python advent_loader.py <input.csv>")
        print("\nCSV 컬럼: 이름, 성전, 기수, 내용")
        sys.exit(1)

    csv_path = sys.argv[1]

    # CSV 읽기
    df = pd.read_csv(csv_path)
    print(f"Loaded {len(df)} rows from {csv_path}")
    print(f"Columns: {list(df.columns)}")

    # 컬럼명 확인
    required_cols = ["이름", "성전", "기수", "내용"]
    for col in required_cols:
        if col not in df.columns:
            print(f"Error: Missing column '{col}'")
            print(f"Available columns: {list(df.columns)}")
            sys.exit(1)

    # AI 클라이언트
    client = OpenAI(api_key=OPENAI_API_KEY)

    # DB 연결
    conn = psycopg2.connect(
        host=DB_HOST,
        port=DB_PORT,
        dbname=DB_NAME,
        user=DB_USER,
        password=DB_PASSWORD
    )

    try:
        for idx, row in df.iterrows():
            name = str(row["이름"]).strip()
            temple = str(row["성전"]).strip()
            batch = int(row["기수"])
            resolution = str(row["내용"]).strip()

            print(f"\n[{idx+1}/{len(df)}] Processing: {name} ({temple}, {batch}기)")
            print(f"  내용: {resolution[:50]}...")

            # AI 추천
            print("  Getting AI recommendations...")
            verses = get_ai_recommendations(client, resolution)

            # DB 적재
            insert_to_db(conn, name, temple, batch, verses)

    finally:
        conn.close()

    print(f"\n✅ Done! Processed {len(df)} people.")


if __name__ == "__main__":
    main()
