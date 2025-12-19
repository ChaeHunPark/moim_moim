#!/bin/sh

# -e 옵션: 스크립트 실행 중 하나라도 실패(exit code != 0)하면 즉시 종료
set -e

# 첫 번째 인자(host:port 형태)를 hostport 변수에 저장한다.
hostport=$1

# $1(첫 번째 인자)을 제외하고 나머지는 실행할 명령어로 간주
shift

# 이후 모든 인자는 실제로 실행할 커맨드가 된다. 예: java -jar app.jar
cmd="$@"

# hostport에서 콜론(:) 앞부분만 잘라 host 변수로 저장
host=$(echo $hostport | cut -d: -f1)

# 콜론(:) 뒷부분을 port 변수로 저장
port=$(echo $hostport | cut -d: -f2)

# 해당 host:port에 접속될 때까지 반복해서 체크
until nc -z $host $port; do
  echo "Waiting for $host:$port..."
  sleep 2   # 2초마다 재시도
done

# DB 또는 대상 서비스가 열리면 메시지 출력
>&2 echo "$host:$port is available, starting command..."

# DB가 준비되었으므로 실제 실행할 명령어(cmd)를 실행한다.
# exec는 현재 쉘을 대체하므로 프로세스 PID가 바뀌지 않아서 컨테이너 관리에 유리함.
exec $cmd
