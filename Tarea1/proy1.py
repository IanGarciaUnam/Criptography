"""
Python version 3.8.5

@author: López Pavón Daniela
@version 1.0.0

Proyecto de Caesar Cipher Cracking
Descifrado del código César

"""
import os.path as path
import sys
class Caracter:
	"""
	Clase que nos permite modelar caracteres y frecuencia
	de aparición
	"""
	def __init__(self, caracter, aparicion):
		"""
		Constructor de clase caracteres
		PARAMS: 
			caracter : char
			aparicion: int
		"""
		self.caracter=caracter
		self.caracter_value=ord(caracter)
		self.aparicion=aparicion

	def set_caracter(self, caracter):
		"""
		Modifica el caracter del objeto
		PARAM: 
			caracter: chr
		"""
		self.caracter=caracter
	def get_caracter(self):
		"""
		Regresa el caracter del objeto
		RETURNS:
			self.caracter:chr
		"""
		return self.caracter

	def set_caracter_per_value(self, caracter_value):
		self.caracter_value=caracter_value
		self.carater=chr(self.caracter_value)

	def agrega(self):
		"""
		Suma 1 a la aparición del caracter
		"""
		self.aparicion=self.aparicion+1

	def agrega(self, value):
		"""
		Modifica el valor de aparición
		del caracter
		"""
		self.aparición=value

	def __gt__(self, caracter):
		"""
		Permite comparar y ordenar los caracteres en orden de aparición
		PARAMS:
			caracter:Caracter
		"""
		return self.aparicion>caracter.aparicion

	def __eq__(self, char):
		"""
		Evalúa si char es un objeto tipo Caracter
		y si es igual a otro
		PARAM:
			char: Objeto tipo Caracter
		"""
		if isinstance(char, Caracter):
			return self.caracter_value==char.caracter_value
	def __str__(self):
		return str(self.caracter)

class Ejecucion:
	"""
	Esta clase modela algunos de los procesos comunes de ejecución
	como lectura de texto y conteo
	"""

	def __init__(self, archivo):
		"""
		Constructor de la clase ejecución
		y ejecución de la misma, tras la lectura del archivo de texto
		realiza multiples  conversiones, en lista de palabras, y esta asu vez en lista
		de carácteres, de donde se obtiene la distancia en referencia de la E,A,O, si esta es coincidente 
		y por lo tanto descifrable, si este lo es, será impreso el resultado del proceso de descifrado en la pantalla, en otro 
		caso, se enviará un aviso y el programa terminará
		PARAMS:
			arch: archivo
		"""
		self.archivo=archivo
		self.lectura(True)
		self.conversion()
		self.conversion_lista_caracteres()
		if self.is_descifrable(self.Char_List):
			print(self.descifra())
		else:
			print("[ERROR]-- NO DESCIFRABLE, INCONGRUENCIA EN VALORES")


	def my_split(self, word):
		"""
		Separa una cadena de texto en caracteres
		PARAM: 
			word: str
		RETURNS:
			list:chr
		"""
		return [char for char in word]

	def str_list(self, lista):
		"""
		Método auxiliar para imprimir una lista de Caracteres
		PARAM:
			lista:Caracter
		"""
		out_chain="[ "
		for x in lista:
			out_chain+=str(x)+"--"+ str(x.aparicion)+","
		return out_chain+"]"


	def lectura(self, flag):
		"""
		Lee el archivo y devuelve una lista separa por "\n"
		Param:
			flag: boolean, IF flag is true, every char will be normalized as
			lowercase otherwise  it will be keep as in the original
		"""
		self.content:str=""
		self.content_list=[]
		if not path.exists(self.archivo) or not path.isfile(self.archivo):
			print("[ERROR] --No such file ")
			sys.exit(-1)

		with open(self.archivo,"r") as file:
			for line in file:
				if flag:
					self.content+=line.lower()
				else:
					self.content+=line
		self.content_list.extend(self.content.split())

	def conversion(self):
		"""
		Obtenemos una lista de caracteres
		"""
		self.character_list=[]
		for word in self.content_list:
			self.character_list.extend(self.my_split(word))

	def conversion_lista_caracteres(self):
		"""
		Convierte la self.character_list:list:chr en la
		self.Char_List:list:Caracter
		"""
		if self.character_list==None:
			print("character_list is empty")
			return
		self.Char_List=[]
		for c in self.character_list:
			if not self.busca_Caracter(self.Char_List, Caracter(c, self.character_list.count(c))):
				self.Char_List.append(Caracter(c, self.character_list.count(c)))

	def busca_Caracter(self, lista, Caracter_Comparable):
		"""
		Busca el caracter en una lista de Caracteres
		PARAMS:
			lista: list:Caracter
			Caracter_Comparable: Caracter
		RETURNS:
			boolean 
			True if Caracter_Comparable is in lista
			| False otherwhise

		"""
		for x in lista:
			if x==Caracter_Comparable:
				return True
		return False

	def is_descifrable(self, lista):
		"""
		Recordemos que debemos encontrar los elementos de mayor porcentaje
		dado que 
		E corresponde al 13%
		A corresponde al 12%
		O corresponde al 8%
		y nombra a la variable self.distancia en caso de que sea verdadero
		PARAMS:
			lista: list:Caracter
		"""
		self.Char_List.sort(reverse=True)
		E=self.Char_List[0]
		A=self.Char_List[1]
		O=self.Char_List[2]
		distancia_e_x=E.caracter_value-ord('e')
		distancia_a_x=A.caracter_value-ord('a')
		distancia_o_x=O.caracter_value-ord('o')
		if distancia_e_x==distancia_a_x and distancia_a_x==distancia_o_x:
			self.distancia=distancia_e_x
			return True
		return False

	def descifra(self):
		"""
		Aplica el proceso de descifrado
		"""
		self.lectura(False)
		lista_lines=self.content.split("\n")
		salida_txt=""
		for line in lista_lines:
			lista_words=line.split(" ")
			for word in lista_words:
				lista_chars=self.my_split(word)
				for char in lista_chars:
						salida_txt+=chr(ord(char)-self.distancia)
				salida_txt+=" "
			salida_txt+="\n"
		return salida_txt







"""
if len(sys.argv)>0:
	try:
		E=Ejecucion(sys.argv[1])
	except IndexError:
		print("USAGE python proy1.py archivo-de-texto-cifrado.x")
	except:
		print("A ocurrido un error, verifica que tu archivo contenga el formato de codificación UTF-8")			
else:
	print("USAGE python proy1.py archivo-de-texto-cifrado.x")
"""









