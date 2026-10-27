"""清理 Sa-Token 权限缓存（erp:auth:*）。

新菜单/新权限串入库后，权限缓存最长 30 分钟才过期，
非超管账号在缓存过期前看不到新权限。本脚本直接连 Redis 删除缓存 key。
"""
import socket
import sys

HOST, PORT, PASSWORD = "192.168.31.199", 6379, "123456"


def read_line(sock):
    buf = b""
    while not buf.endswith(b"\r\n"):
        chunk = sock.recv(1)
        if not chunk:
            break
        buf += chunk
    return buf[:-2]


def read_reply(sock):
    line = read_line(sock)
    if not line:
        return None
    tag, rest = line[:1], line[1:]
    if tag == b"+":
        return rest.decode()
    if tag == b"-":
        return RuntimeError(rest.decode())
    if tag == b":":
        return int(rest)
    if tag == b"$":
        n = int(rest)
        if n == -1:
            return None
        data = b""
        while len(data) < n + 2:
            data += sock.recv(n + 2 - len(data))
        return data[:n].decode()
    if tag == b"*":
        n = int(rest)
        return [read_reply(sock) for _ in range(n)] if n >= 0 else None
    return rest


def main():
    sock = socket.create_connection((HOST, PORT), timeout=5)
    print("AUTH:", send(sock, "AUTH", PASSWORD))

    patterns = ["erp:auth:*", "Authorization:login:*"]
    total = 0
    for pattern in patterns:
        keys = send(sock, "KEYS", pattern)
        if isinstance(keys, RuntimeError):
            print(f"KEYS {pattern} 失败:", keys)
            continue
        keys = keys or []
        print(f"匹配 {pattern}: {len(keys)} 个")
        for key in keys:
            print("  DEL", key, "->", send(sock, "DEL", key))
        total += len(keys)

    print(f"共清理 {total} 个 key")
    sock.close()
    return 0


def send(sock, *args):
    cmd = f"*{len(args)}\r\n"
    for a in args:
        a = str(a)
        cmd += f"${len(a.encode('utf-8'))}\r\n{a}\r\n"
    sock.sendall(cmd.encode("utf-8"))
    return read_reply(sock)


if __name__ == "__main__":
    sys.exit(main())
