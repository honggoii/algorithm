import java.io.*;
import java.util.*;

public class Main {
    static int n, m, f; // nxn, 손님 m명, 시작 연료양
    static int[][] board; // 맵 정보
    static boolean[][] visited; // 방문 확인
    static Taxi taxi;
    static Guest[] guest; // 손님
    static boolean[] finished; // 손님 다 데려다 줬는지 확인
    static int[] dx = {-1, 1, 0, 0}; // 상 하
    static int[] dy = {0, 0, -1, 1}; // 좌 우

    // 택시 클래스
    static class Taxi {
        int x; // 행
        int y; // 열
        int fuel; // 연료

        Taxi(int x, int y, int fuel) {
            this.x = x;
            this.y = y;
            this.fuel = fuel;
        }
    }

    // 손님 클래스
    static class Guest {
        int x; // 현재 행
        int y; // 현재 열
        int dex; // 목적지 행
        int dey; // 목적지 열

        Guest() {}

        Guest(int x, int y, int dex, int dey) {
            this.x = x;
            this.y = y;
            this.dex = dex;
            this.dey = dey;
        }
   }

    // 좌표 클래스
    static class Coordinate {
        int x; // 행
        int y; // 열
        int used; // 사용한 연료

        Coordinate() {}

        Coordinate(int x, int y, int used) {
            this.x = x;
            this.y = y;
            this.used = used;
        }
    }

    static void input() throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer tokens = new StringTokenizer(br.readLine());
        n = toInt(tokens.nextToken());
        m = toInt(tokens.nextToken());
        f = toInt(tokens.nextToken());
        board = new int[n+1][n+1];
        visited = new boolean[n+1][n+1];
        guest = new Guest[m];
        finished = new boolean[m];

        // n x n 정보 입력
        // 0: 빈칸, 1: 벽
        for (int i=1; i<=n; i++) {
            tokens = new StringTokenizer(br.readLine());
            for (int j=1; j<=n; j++) {
                board[i][j] = toInt(tokens.nextToken());
            }
        }

        // 택시 시작 행과 열 입력
        tokens = new StringTokenizer(br.readLine());
        taxi = new Taxi(toInt(tokens.nextToken()), toInt(tokens.nextToken()), f);

        // 손님 정보 입력
        for (int i=0; i<m; i++) {
            tokens = new StringTokenizer(br.readLine());
            int x = toInt(tokens.nextToken());
            int y = toInt(tokens.nextToken());
            int dex = toInt(tokens.nextToken());
            int dey = toInt(tokens.nextToken());
            guest[i] = new Guest(x, y, dex, dey);
            board[x][y] = i+10;
        }
    }

    static void move() {
        int cnt = 0;
        while(true) {
            if (m <= cnt) break; // 다 데려다줬으면
            initVisit(); // 방문 배열 초기화
            int now = findCloser(); // 현재 위치에서 가장 가까운 손님 찾기
            if (now == -1) return;
            initVisit(); // 방문 배열 초기화
            boolean success = moveDestination(now); // 손님 목적지로 이동
            if (!success) return;
            cnt++;
        }
    }

    static void initVisit() {
        for (int i=1; i<=n; i++) {
            for (int j=1; j<=n; j++) {
                visited[i][j] = false;
            }
        }
    }

    static class Tmp implements Comparable<Tmp> {
        int num; // 손님 번호
        int d; // 이동 거리

        Tmp(int num, int d) {
            this.num = num;
            this.d = d;
        }

        @Override
        public int compareTo(Tmp t) {
            if (this.d == t.d) return this.num - t.num;
            return this.d - t.d;
        }
    }

    static int findCloser() {
        Queue<Coordinate> q = new LinkedList<>();
        q.add(new Coordinate(taxi.x, taxi.y, 0));
        visited[taxi.x][taxi.y] = true;
        ArrayList<Tmp> tmp = new ArrayList<>();
        while (!q.isEmpty()) {
            Coordinate c = q.poll();
            // 손님이 있는 좌표면
            if (10 <= board[c.x][c.y]) {
                tmp.add(new Tmp(board[c.x][c.y]-10, c.used));
            }

            for (int i=0; i<4; i++) {
                int nx = c.x + dx[i];
                int ny = c.y + dy[i];

                // 범위 내에 있어야 하고
                if (range(nx, ny)) {
                    // 방문한 적 없고 벽이 아니면
                    if (!visited[nx][ny] && (board[nx][ny] != 1)) {
                        q.add(new Coordinate(nx, ny, c.used+1));
                        visited[nx][ny] = true;
                    }
                }
            }
        }

        int d = 0;
        int num = 0;
        if (tmp.size() == 0) return -1;
        else if (0 < tmp.size()){
            Collections.sort(tmp);
            d = tmp.get(0).d;
            num = tmp.get(0).num;
            for (int i=1; i<tmp.size(); i++) {
                if (d < tmp.get(i).d) break;
                else {
                    if (guest[tmp.get(i).num].x == guest[num].x) {
                        if (guest[tmp.get(i).num].y < guest[num].y) {
                            num = tmp.get(i).num;
                        }
                    } else if (guest[tmp.get(i).num].x < guest[num].x){
                        num = tmp.get(i).num;
                    }
                }
            }
        }

        taxi.fuel -= d;
        return num;
    }

    static boolean moveDestination(int now) {
        Queue<Coordinate> q = new LinkedList<>();
        int x = guest[now].x;
        int y = guest[now].y;
        int dex = guest[now].dex;
        int dey = guest[now].dey;
        board[x][y] = 0;

        q.add(new Coordinate(x, y, 0));
        visited[x][y] = true;

        while (!q.isEmpty()) {
            Coordinate c = q.poll();
            // 목적지면
            if ((c.x == dex) && (c.y == dey)) {
                // 현재 택시 연료보다 사용한 연료가 더 작으면
                if (c.used <= taxi.fuel) {
                    // 손님 이동 완료
                    finished[now] = true;
                    taxi.fuel += c.used; // 연료 채우기
                    taxi.x = dex;
                    taxi.y = dey;
                    break;
                }
                 else {
                    return false;
                }
            }

            for (int i=0; i<4; i++) {
                int nx = c.x + dx[i];
                int ny = c.y + dy[i];

                if (range(nx, ny)) {
                    if (!visited[nx][ny] && (board[nx][ny] != 1)) {
                        q.add(new Coordinate(nx, ny, c.used+1));
                        visited[nx][ny] = true;
                    }
                }
            }
        }

        return true;
    }

    static void checkFinish() {
        for (int i=0; i<m; i++) {
            if (!finished[i]) {
                System.out.println(-1); // 한 명이라도 안 데려다 줬으면 -1
                return;
            }
        }
        // 다 데려다 줬으면
        System.out.println(taxi.fuel); // 남은 연료양 출력
    }

    static int toInt(String s) {
        return Integer.parseInt(s);
    }

    static boolean range(int nx, int ny) {
        return 1 <= nx && nx <= n && 1 <= ny && ny <= n;
    }

    public static void main(String[] args) throws IOException {
        input(); // 입력
        move(); // 이동
        checkFinish(); // 손님 다 데려다줬는지 확인
        return;
    }
}
