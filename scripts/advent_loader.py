#!/usr/bin/env python3
"""
어드벤트 캘린더 데이터 적재 스크립트

사용법:
1. 구글 스프레드시트에서 CSV로 다운로드 (이름, 성전, 기수, 내용 컬럼)
2. .env 파일에 OPENAI_API_KEY 설정 (또는 환경변수로)
3. python advent_loader.py input.csv
4. 생성된 output.sql을 DB에서 실행

필요한 패키지:
pip install openai
"""

import os
import sys
import json
import re
import csv
from datetime import datetime
from openai import OpenAI

# 환경변수에서 API 키 로드 (dotenv 없이도 동작)
try:
    from dotenv import load_dotenv
    load_dotenv()
except ImportError:
    pass  # dotenv 없어도 환경변수에서 읽음

OPENAI_API_KEY = os.getenv("OPENAI_API_KEY")

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

# AI가 잘못된 형식으로 반환할 때 변환 매핑
BOOK_NAME_ALIASES = {
    "1_SAMUEL": "FIRST_SAMUEL", "2_SAMUEL": "SECOND_SAMUEL",
    "1_KINGS": "FIRST_KINGS", "2_KINGS": "SECOND_KINGS",
    "1_CHRONICLES": "FIRST_CHRONICLES", "2_CHRONICLES": "SECOND_CHRONICLES",
    "1_CORINTHIANS": "FIRST_CORINTHIANS", "2_CORINTHIANS": "SECOND_CORINTHIANS",
    "1_THESSALONIANS": "FIRST_THESSALONIANS", "2_THESSALONIANS": "SECOND_THESSALONIANS",
    "1_TIMOTHY": "FIRST_TIMOTHY", "2_TIMOTHY": "SECOND_TIMOTHY",
    "1_PETER": "FIRST_PETER", "2_PETER": "SECOND_PETER",
    "1_JOHN": "FIRST_JOHN", "2_JOHN": "SECOND_JOHN", "3_JOHN": "THIRD_JOHN",
    "I_SAMUEL": "FIRST_SAMUEL", "II_SAMUEL": "SECOND_SAMUEL",
    "I_KINGS": "FIRST_KINGS", "II_KINGS": "SECOND_KINGS",
    "I_CHRONICLES": "FIRST_CHRONICLES", "II_CHRONICLES": "SECOND_CHRONICLES",
    "I_CORINTHIANS": "FIRST_CORINTHIANS", "II_CORINTHIANS": "SECOND_CORINTHIANS",
    "I_THESSALONIANS": "FIRST_THESSALONIANS", "II_THESSALONIANS": "SECOND_THESSALONIANS",
    "I_TIMOTHY": "FIRST_TIMOTHY", "II_TIMOTHY": "SECOND_TIMOTHY",
    "I_PETER": "FIRST_PETER", "II_PETER": "SECOND_PETER",
    "I_JOHN": "FIRST_JOHN", "II_JOHN": "SECOND_JOHN", "III_JOHN": "THIRD_JOHN",
    "SONG_OF_SOLOMON": "SONG_OF_SONGS",
}


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
        json_match = re.search(r'\[.*\]', response_text, re.DOTALL)
        if json_match:
            verses = json.loads(json_match.group())
        else:
            raise ValueError(f"Failed to parse AI response: {response_text}")

    # 검증 및 변환
    if len(verses) != 28:
        print(f"  Warning: Got {len(verses)} verses instead of 28")

    for v in verses:
        book_name = v["book_name"]
        # 별칭 변환
        if book_name in BOOK_NAME_ALIASES:
            v["book_name"] = BOOK_NAME_ALIASES[book_name]
        # 검증
        if v["book_name"] not in BOOK_NAMES:
            print(f"  Warning: Unknown book name '{book_name}', skipping validation")

    return verses[:28]


def escape_sql(s: str) -> str:
    """SQL 문자열 이스케이프"""
    return s.replace("'", "''")


def generate_sql(person_id: int, name: str, temple: str, batch: int, verses: list[dict]) -> str:
    """INSERT SQL 생성"""

    sql_lines = []

    # AdventPerson INSERT
    sql_lines.append(f"-- {name} ({temple}, {batch}기)")
    sql_lines.append(f"INSERT INTO advent_persons (id, name, temple, batch) VALUES ({person_id}, '{escape_sql(name)}', '{escape_sql(temple)}', {batch});")

    # AdventVerse INSERT
    for idx, v in enumerate(verses, start=1):
        verse_id = (person_id - 1) * 28 + idx
        sql_lines.append(
            f"INSERT INTO advent_verses (id, person_id, sequence, book_name, chapter, verse) "
            f"VALUES ({verse_id}, {person_id}, {idx}, '{v['book_name']}', {v['chapter']}, {v['verse']});"
        )

    sql_lines.append("")
    return "\n".join(sql_lines)


def read_csv(csv_path: str) -> list[dict]:
    """CSV 파일 읽기"""
    rows = []
    with open(csv_path, "r", encoding="utf-8") as f:
        reader = csv.DictReader(f)
        for row in reader:
            rows.append(row)
    return rows


def main():
    if len(sys.argv) < 2:
        print("Usage: python advent_loader.py <input.csv>")
        print("\nCSV 컬럼: 이름, 성전, 기수, 내용")
        sys.exit(1)

    csv_path = sys.argv[1]
    output_path = csv_path.replace(".csv", "_output.sql")

    # CSV 읽기
    rows = read_csv(csv_path)
    print(f"Loaded {len(rows)} rows from {csv_path}")

    if rows:
        print(f"Columns: {list(rows[0].keys())}")

    # 컬럼명 확인
    required_cols = ["이름", "성전", "기수", "내용"]
    if rows:
        for col in required_cols:
            if col not in rows[0]:
                print(f"Error: Missing column '{col}'")
                print(f"Available columns: {list(rows[0].keys())}")
                sys.exit(1)

    # AI 클라이언트
    if not OPENAI_API_KEY:
        print("Error: OPENAI_API_KEY 환경변수를 설정해주세요")
        sys.exit(1)

    client = OpenAI(api_key=OPENAI_API_KEY)

    # SQL 파일 생성
    with open(output_path, "w", encoding="utf-8") as f:
        f.write("-- 어드벤트 캘린더 데이터 적재 SQL\n")
        f.write(f"-- 생성일: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}\n\n")

        for idx, row in enumerate(rows):
            name = str(row["이름"]).strip()
            temple = str(row["성전"]).strip()
            batch = int(row["기수"])
            resolution = str(row["내용"]).strip()

            print(f"\n[{idx+1}/{len(rows)}] Processing: {name} ({temple}, {batch}기)")
            print(f"  내용: {resolution[:50]}...")

            # AI 추천
            print("  Getting AI recommendations...")
            verses = get_ai_recommendations(client, resolution)
            print(f"  ✓ Got {len(verses)} verses")

            # SQL 생성
            person_id = idx + 1
            sql = generate_sql(person_id, name, temple, batch, verses)
            f.write(sql)

    print(f"\n✅ Done! SQL saved to: {output_path}")
    print(f"\n실행 방법:")
    print(f"  1. SSH 터널링으로 DB 연결")
    print(f"  2. psql 또는 DB 클라이언트에서 SQL 실행")
    print(f"     psql -h localhost -p 5432 -U dbmasteruser -d <dbname> -f {output_path}")


if __name__ == "__main__":
    main()
