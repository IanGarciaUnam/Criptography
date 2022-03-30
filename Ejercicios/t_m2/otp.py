import random
def enc(key, message):
	ciphered_message=[chr(ord(m)^ord(k)) for m,k in zip(message,key)]
	return "".join(ciphered_message)

def dec(key, ciphered):
	desciphered_message=[chr(ord(k)^ord(c)) for k,c in zip(key, ciphered)]
	return "".join(desciphered_message)

def enc_arch(key,message:bytes):
	encoded=bytearray()
	i=0
	while i < len(message)-1:
		k=ord(key[i])
		encoded.append(message[i]^k)
		i+=1
	return encoded

def dec_arch(key, message:bytes):
	decoded=bytearray()
	i=0
	while i < len(message)-1:
		k=ord(key[i])
		decoded.append(k ^ message[i])
		i+=1
	return decoded

def check(c1, c2):
	for w,z in zip(c1,c2):
		if w!=z:
			return False
	return True


def generate_random_key(tamano:int):
	x=''
	for i in range(tamano):
		x+=chr(random.randint(0,255))
	return x

archivo="archivo.txt"
f=open(archivo, "rb")

contenido= f.read()
key=generate_random_key(len(contenido))
contenido_cifrado=enc_arch(key, contenido)
contenido_descifrado=dec_arch(key, contenido_cifrado)
archivo_for_writing="archivo1.txt"
f_w=open(archivo_for_writing, "wb")
f_w.write(contenido_descifrado)
f_w.close()
f.close()


ToChekc=check(contenido.strip(), contenido_descifrado.strip())



string="Este es el mensaje previo a la 3a guerra mundia salu2"

k=generate_random_key(len(string))

ToChekc2=enc(k, string)==dec(k,string)


print("Test for encrypting files: ",ToChekc)
print("Test for encrypting str: ", ToChekc2)


