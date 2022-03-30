import Cifrado_cesar as cesar

"""
Módulo que implementa el cifrado y descifrado de Vigenere.
A su vez implementael cifrado y descifrado para
archivos cualquiera.
"""
alfabeto = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"

"""
Función que cifra un texto con el método de Vigenere.
Recibe una palabra clave y un texto a cifrar.
"""
def vigenere_enc(keyword,texto):
    claves = empata_palabra(keyword,texto)
    cript_text = ""
    for pair in claves:
        key_c = alfabeto.index(pair[0])
        cifr = cesar.EncCesar(key_c,pair[1])
        cript_text += cifr

    return cript_text


"""
Función que descifra un texto con el método de vigenere.
Recibe la palabra clave que se usó para cifrar y el texto
cifrado.
"""
def vigenere_dec(keyword,texto):
    clave = empata_palabra(keyword,texto)
    decript_tex = ""
    for pair in clave:
        key_d = alfabeto.index(pair[0])
        decifr = cesar.EncCesar(-key_d,pair[1])
        decript_tex += decifr

    return decript_tex

"""
Función que empalma la palabra clave recibida con el mensaje.
"""
def empata_palabra(palabra,texto):
    nuevs = texto.replace(" ","")
    tam = len(palabra)
    empalme = list()
    i = 0
    for letra in nuevs:
        pos = i % tam
        clave = palabra[pos]
        elem = clave + letra
        empalme.append(elem)
        i += 1

    return empalme

"""
Función que encripta una secuencia cualquiera de bytes.
Recibe una palabra clave, la secuencia de bytes a encriptar.
"""
def vig_enc_arch(keyword,texto):
    enc_vig_arch = bytearray()
    claves = empata_byte(keyword,texto)
    for pair in claves:
        cifr = cesar.cesar_archivo(pair[0],pair[1].to_bytes(1,'big'))
        enc_vig_arch += (cifr)

    return bytes(enc_vig_arch)


"""
Función que desencripta una secuencia de bytes.
Recibe una palabra clave y la secuencua de bytes a desencriptar.
"""
def vig_dec_arch(keyword,texto):
    decript = bytearray()
    clave = empata_byte(keyword,texto)
    for pair in clave:
        decifr = cesar.cesar_archivo(-pair[0],pair[1].to_bytes(1,'big'))
        decript += decifr

    return bytes(decript)

"""
Función que empata los bytes de una palabra clave
con los de un texto recibido.
"""
def empata_byte(keybyte,byte_tex):
    tam = len(keybyte)
    empalme = list()
    i = 0
    for letra in byte_tex:
        pos = i % tam
        clave = keybyte[pos].to_bytes(1,'big')
        elem = clave + letra.to_bytes(1,'big')
        empalme.append(elem)
        i += 1

    return empalme
