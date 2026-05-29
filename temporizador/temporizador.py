import tkinter as tk
from tkinter import messagebox

puntaje = 0
tiempo_restante = 75  # 1 minuto 15 segundos

root = tk.Tk()
root.title("Pokemon Clicker - Temporizador")

label_tiempo = tk.Label(root, text="01:15", font=("Arial", 30))
label_tiempo.pack(pady=20)

def actualizar_temporizador():
    global tiempo_restante
    minutos = tiempo_restante // 60
    segundos = tiempo_restante % 60
    label_tiempo.config(text=f"{minutos:02d}:{segundos:02d}")

    if tiempo_restante > 0:
        tiempo_restante -= 1
        root.after(1000, actualizar_temporizador)
    else:
        finalizar_juego()

def finalizar_juego():
    global puntaje
    if puntaje <= 0:
        resultado = "PERDISTE"
    elif puntaje >= 30:
        resultado = "¡GANASTE!"
    else:
        resultado = "JUEGO TERMINADO"

    messagebox.showinfo("Fin del Juego", f"{resultado}\nTu puntaje: {puntaje}")
    root.destroy()

# Arranca automáticamente el temporizador
actualizar_temporizador()
root.mainloop()