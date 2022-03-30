alfabeto = 'ABCDEFGHIJKLMNÑOPQRSTUVWXYZ'
N = len(alfabeto)


def Enc_afin(key1, key2, message):
    c=''
    k1=key1
    k2=key2
    for letter in message:
        ci= alfabeto[((k1*ord(letter))+k2)%N]
        c+=ci
    return c

def Dec_afin(key1, key2, message):
    c=''
    k1=key1
    k2=key2
    for letter in message:
        value,n=get_(k1,N)
        ci=alfabeto[(((ord(letter)-k2)*value) % N)]
        c+=ci
    return c


def get_gcd(a, b):
    k = a // b
    remainder = a % b
    while remainder != 0:
        a = b 
        b = remainder
        k = a // b
        remainder = a % b
    return b

# Algoritmo euclidiano mejorado para encontrar xey de ecuaciones lineales
def get_(a, b):
    if b == 0:
        return 1, 0
    else:
        k = a // b
        remainder = a % b       
        x1, y1 = get_(b, remainder)
        x, y = y1, x1 - k * y1          
    return x, y

    a, b = input().split()
    a, b = int(a), int(b)

    #Guarde el valor absoluto de la b inicial
    if b < 0:
        m = abs(b)
    else:
        m = b
    flag = get_gcd(a, b)

    # Determine si el máximo común divisor es 1, si no, no hay elemento inverso
    if flag == 1:   
        x, y = get_(a, b)   
        x0 = x % m #Para Python '%' es la operación de módulo, por lo que no se necesita '+ m'
        print(x0) # x0 es el inverso requerido
    else:
        print("Do not have!")

def Enc(key, message):
    c = ''
    i=0
    for letter in message:
        if ord(letter)==32 or letter=='\n' or not letter in alfabeto:
            c+=" "
            continue
        new_index = (alfabeto.index(letter) + key) % N
        ci = alfabeto[new_index]
        if i%4==0:
            ci+=" "
        c += ci
        i+=1
    return c

def Enc_ascii(key, message):
    c=''
    for letter in message:
        new_index=(ord(letter)+key)%256
        ci=chr(new_index)
        c+=ci
    return c

def Dec(key, ciphertext):
    return Enc(-key, ciphertext)
    #return Enc_ascii(-key, ciphertext)

#message="HVWRB HP WRGR SHVH D HVWDU HP PDGD"
f=open("arch.txt", "r+")
x=f.read()
print(x)
print("============================================================================")
f.close()
m=""

for i in [ 14, 21, 28, 35 , 26]:
    for j in [ 14, 21, 28, 35 , 26]:
        print(i,j)
        m+="\n==================================="+str(i)+"=======================================\n"
        m+=Dec_afin(i,j, x)+"\n"
        print(m)



nf=open("out.txt", "a+")
nf.write(m)
nf.close()

"""
nf=open("out.txt", "a+")

for i in range(100):
    m_c=Dec(i, x)
    nt=nf.read()
    nf.write(nt+"\n"+ str(i)+":\n"+m_c)
nf.close()
"""