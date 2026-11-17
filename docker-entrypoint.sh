#!/bin/sh
# Punto de entrada para la app Swing de escritorio.
# Inicia Xvfb (servidor X virtual) para proveer un display sin pantalla física,
# luego lanza la aplicación Java.
rm -f /tmp/.X99-lock
Xvfb :99 -screen 0 1024x768x16 -nolisten tcp &
XVFB_PID=$!

# Esperar hasta que Xvfb esté listo (máximo 15 segundos)
for i in $(seq 1 30); do
    if xdpyinfo -display :99 >/dev/null 2>&1; then
        break
    fi
    sleep 0.5
done

export DISPLAY=:99
exec java -jar /app/app.jar
