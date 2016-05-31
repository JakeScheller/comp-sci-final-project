from collections import namedtuple
from random import randrange
from enum import IntEnum

def clearScreen():
	print(chr(27)+"[2J"+chr(27)+"[0;0H")

class Card(object):
	def __init__(self, value, suit):
		self.value = value
		self.suit = suit

	def toString(self):
		if self.suit == "JOKER":
			return self.suit
		valueStr = str(self.value)
		if self.value > 10:
			valueStr = ["JACK", "QUEEN", "KING", "ACE", "JOKER"][self.value-11]
		return valueStr + " of " + self.suit

class CardDeck(object):
	def __init__(self, numDecks):
		self.cards = []
		self.curCardIdx = 0

		for i in range(numDecks):
			for suit in ["HEARTS", "DIAMONDS", "CLUBS", "SPADES"]:
				for value in [2,3,4,5,6,7,8,9,10,11,12,13,14]:
					self.cards.append(Card(value, suit))
			self.cards.append(Card(15, "JOKER"))
			self.cards.append(Card(15, "JOKER"))

	def drawCard(self):
		assert self.curCardIdx < len(self.cards)
		idx = randrange(self.curCardIdx, len(self.cards))
		selectedCard = self.cards[idx]
		self.cards[idx] = self.cards[self.curCardIdx]
		self.cards[self.curCardIdx] = selectedCard
		self.curCardIdx += 1
		return selectedCard

	def getNumCards(self):
		return max(len(self.cards) - self.curCardIdx, 0)

class Game(object):
	def __init__(self):
		self.players = []
		self.deck = CardDeck(2)

	def startGame(self):
		self.doAddPlayersPhase()

	def doAddPlayersPhase(self):
		clearScreen()
		print("Add at least 2 players.")
		print()

		doneAddingPlayers = False
		while not doneAddingPlayers:
			if len(self.players) == 0:
				print("No players have been added.")
			else:
				print("Players:", ", ".join([player.name for player in self.players]))
			print("Pick an action.")
			print("1. Add a new player.")
			print("2. Finish adding players.")

			haveValidInput = False
			while not haveValidInput:
				inp = input("?> ").strip().lower()
				if inp == "1":
					haveValidInput = True
					name = input("Player's name? ")
					self.players.append(Player(name, self))
					print()
				elif inp == "2":
					if len(self.players) < 2:
						print("You must add at least 2 players.")
					else:
						haveValidInput = True
						doneAddingPlayers = True
				elif inp != "":
					print("Invalid input.")

		print()
		print("You have added", len(self.players), "players:", ", ".join([player.name for player in self.players]))
		input("Press enter to continue to the setup phase.")
		self.doSetupPhase()

	def doSetupPhase(self):
		clearScreen()
		print("This game uses 2 decks of cards. That means there are 102 cards in total.")
		print("Each player will now draw six cards to determine their initial stats.")
		print("If a player draws a JOKER, he or she gets to draw an extra card.")
		print()

		for player in self.players:
			print("It is", player.name + "'s", "turn. Only", player.name, "may look at the screen.")
			input("Press enter to start " + player.name + "'s turn.")
			player.drawInitialCards()

		print("Everyone has drawn their initial cards.")
		input("Press enter to continue to the play phase.")
		self.doPlayPhase()

	def doPlayPhase(self):
		clearScreen()
		print("Starting play phase. Have fun!")
		print()

		gameOver = False
		while not gameOver:
			print("The turn order is based on the speed stat. The turn order this round is:")
			turnOrder = list(reversed(sorted(self.players, key=lambda x: (x.speed, x.getTotalResources()))))
			print("\t" + ", ".join([player.name for player in turnOrder]))
			print()
			for player in turnOrder:
				print("It is", player.name + "'s", "turn. Only", player.name, "may look at the screen.")
				input("Press enter to start " + player.name + "'s turn.")
				player.doTurn()
				gameOver = (len(self.players) == 1)
				if gameOver:
					break
			if not gameOver:
				print("The round is over.")
				input("Press enter to start the next round.")
			clearScreen()

		print("The game is over.")
		print("The winner is:", self.players[0].name)
		input("Press enter to quit.")

	def performAttack(self, attacker, victim, attackValue):
		print(attacker.name, "is attacking", victim.name + ". Only", victim.name, "may look at the screen.")
		input("Press enter to continue.")
		clearScreen()
		victim.doWasAttacked(attacker, attackValue)

	def completeAttack(self, attacker, victim, attackValue, defenseValue):
		print(attacker.name, "attacked with a value of", str(attackValue) + ".")
		if defenseValue == 0:
			print(victim.name, "did not defend.")
		else:
			print(victim.name, "defended with a value of", str(defenseValue) + ".")
		if attackValue > defenseValue:
			print(attacker.name + "'s attack was successful.")
			numHealthLost = attackValue - defenseValue
			if numHealthLost > 1:
				print(victim.name, "lost", numHealthLost, "health points.")
			else:
				print(victim.name, "lost", numHealthLost, "health point.")
			victim.health -= numHealthLost
			if victim.health <= 0:
				victim.health = 0
				self.playerDied(attacker, victim)
		else:
			print(victim.name + "'s defense was successful.")
		input("Press enter to end " + attacker.name + "'s turn.")

	def playerDied(self, attacker, victim):
		for idx, player in enumerate(self.players):
			if player == victim:
				self.players.pop(idx)
		print(victim.name, "died!")
		print()
		victim.printStealableCards()
		print()
		if victim.getNumStealableCards() > 0:
			print(attacker.name, "gets to steal 3 attack or defense cards from", victim.name + ".")
			attacker.doStealCardsFromPlayer(victim, 3)
		else:
			print(victim.name, "has no cards to steal.")
			return None

		if victim.getNumStealableCards() == 0:
			print(victim.name, "has no more cards.")
			return None

		if len(self.players) > 1:
			print("Each other player, ordered by their speed stat, gets to steal 1 attack card from", victim.name + ".")
			for player in list(sorted(filter(lambda x: x != attacker, self.players), key=lambda x: (x.speed, x.getTotalResources()))):
				player.doStealCardsFromPlayer(victim, 1)
				if victim.getNumStealableCards() == 0:
					print(victim.name, "has no more cards.")
					return None

		for card in victim.attackCards:
			self.deck.cards.append(card)
		for card in victim.defenseCards:
			self.deck.cards.append(card)

		print("Everyone has stolen their card(s).")
		return None

class Player(object):
	def __init__(self, name, game):
		self.game = game
		self.name = name
		self.attackCards = []
		self.defenseCards = []
		self.health = 15
		self.speed = 0

	def getTotalResources(self):
		return self.health + self.speed + sum([card.value for card in self.attackCards]) + sum([card.value for card in self.defenseCards])

	def getNumStealableCards(self):
		return len(self.attackCards) + len(self.defenseCards)

	def printStealableCards(self):
		print(self.name + "'s cards:")
		print("\tCLUBS (attack):")
		attackValues = [str(value) for value in sorted([card.value for card in self.attackCards])]
		if len(attackValues) > 0:
			print("\t\t" + ", ".join(attackValues))
		else:
			print("\t\tNo cards of this suit")
		print("\tDIAMONDS (defense):")
		defenseValues = [str(value) for value in sorted([card.value for card in self.defenseCards])]
		if len(defenseValues) > 0:
			print("\t\t" + ", ".join(defenseValues))
		else:
			print("\t\tNo cards of this suit")

	def drawInitialCards(self):
		clearScreen()
		print("You are " + self.name + ".")
		print("You will now draw six cards and add each card to your stat values")
		print("You start with no attack cards, no defense cards, 15 health, and 0 speed.")
		self.drawCards(6)
		print("You are finished drawing cards.")
		print("You will now discard your HEARTS and SPADES cards because their values have already been added to your stats.")
		print()
		self.printStats()
		input("Press enter to finish your turn.")
		clearScreen()

	def drawCards(self, numCards):
		while numCards > 0:
			card = self.game.deck.drawCard()
			if not card:
				print("\tOh no! The deck is out of cards!")
				print("\tYou don't get to draw any more cards.")
				numCards = 0
			else:
				if card.suit == "JOKER":
					print("\tYou drew a JOKER, so you get to draw an extra card. (Jokers are not playable cards.)")
					numCards += 1
				else:
					print("\tYou drew a " + card.toString() + ".", end="")
					if card.suit == "HEARTS":
						print(" This adds " + str(card.value) + " points to your health.")
					elif card.suit == "SPADES":
						print(" This adds " + str(card.value) + " points to your speed.")
					else:
						print()
					self.addCardToStats(card)
					numCards -= 1

	def printStats(self):
		print("Your stats:")
		print("\tDIAMONDS (defense):")
		defenseValues = sorted([card.value for card in self.defenseCards])
		if len(defenseValues) > 0:
			print("\t\t" + ", ".join([str(value) for value in defenseValues]))
			print("\t\tTotal:", sum(defenseValues))
		else:
			print("\t\tNo cards of this suit")
			print("\t\tTotal: 0")
		print("\tCLUBS (attack):")
		attackValues = sorted([card.value for card in self.attackCards])
		if len(attackValues) > 0:
			print("\t\t" + ", ".join([str(value) for value in attackValues]))
			print("\t\tTotal:", sum(attackValues))
		else:
			print("\t\tNo cards of this suit")
			print("\t\tTotal: 0")
		print("\tHEARTS (health):")
		print("\t\tTotal:", self.health)
		print("\tSPADES (speed):")
		print("\t\tTotal:", self.speed)

	def addCardToStats(self, card):
		if card.suit == "HEARTS":
			self.health += card.value
		elif card.suit == "DIAMONDS":
			self.defenseCards.append(card)
		elif card.suit == "CLUBS":
			self.attackCards.append(card)
		elif card.suit == "SPADES":
			self.speed += card.value

	def doTurn(self):
		clearScreen()
		print("You are " + self.name + ".")
		self.printStats()
		print()

		inp = ""
		turnFinished = False
		while not turnFinished:
			print("What would you like to do?")
			print("1. Attack a player")
			print("2. Draw a card")
			print("3. Finish turn")

			haveValidInput = False
			while not haveValidInput:
				inp = input("?> ").strip().lower()
				if inp == "1":
					haveValidInput = True
					if len(self.attackCards) == 0:
						print("You have no attack cards. You can not attack.")
						print()
					else:
						result = self.doAttack()
						if result == True:
							turnFinished = True
				elif inp == "2":
					haveValidInput = True
					if self.game.deck.getNumCards() == 0:
						print("The deck has no cards, so you cannot draw one.")
						print()
					else:
						turnFinished = True
						self.doDrawCard()
				elif inp == "3":
					haveValidInput = True
					turnFinished = True
					print("Finishing turn.")
				elif inp != "":
					print("Invalid input.")

		if inp != "1":
			input("Press enter to finish your turn.")
		clearScreen()

	def doDrawCard(self):
		print("You will now draw one card.")
		self.drawCards(1)
		print("You are finished drawing cards.")
		print()
		self.printStats()

	def doAttack(self):
		attackValues = list(sorted([card.value for card in self.attackCards]))
		attackValues = [str(value) for value in attackValues]
		print("You have these attack cards:", ", ".join(attackValues))
		print("Which card would you like to attack with? (enter 'q' to cancel)")
		attackValue = None
		while attackValue == None:
			inp = input("?> ").strip().lower()
			if inp in attackValues:
				attackValue = int(inp)
			elif inp == "q":
				print()
				return False
			elif inp != "":
				try:
					int(inp)
					print("You don't have that card.")
				except:
					print("Invalid input.")
		print("You are attacking with a value of", attackValue)
		enemy = None
		print("Who would you like to attack? (enter 'q' to cancel)")
		enemies = list(filter(lambda x: x != self, self.game.players))
		enemiesNames = [player.name for player in enemies]
		for idx, enemyName in enumerate(enemiesNames):
			print(str(idx+1) + ".", enemyName)
		while enemy == None:
			inp = input("?> ").strip().lower()
			if inp == "q":
				print()
				return False
			elif inp != "":
				inpNum = None
				try:
					inpNum = int(inp)
				except:
					pass
				if inpNum != None and 0 < inpNum <= len(enemiesNames):
					enemy = enemies[inpNum-1]
				else:
					print("Invalid input.")
					continue
		print("You are attacking", enemy.name, "with a value of", str(attackValue) + ".")
		for idx, card in enumerate(self.attackCards):
			if card.value == attackValue:
				self.attackCards.pop(idx)
				self.game.deck.cards.append(card)
				break
		else:
			assert False
		input("Press enter to perform the attack.")
		clearScreen()
		self.game.performAttack(self, enemy, attackValue)
		return True

	def doWasAttacked(self, attacker, attackValue):
		print("You are", self.name)
		print(attacker.name, "is attacking you!")
		self.printStats()
		print()
		defenseValue = 0
		if len(self.defenseCards) == 0:
			print("You have no defense cards. You must endure the attack.")
		else:
			defenseValues = sorted([card.value for card in self.defenseCards])
			defenseValues = [str(value) for value in defenseValues]
			print("You have these defense cards:", ", ".join(defenseValues))
			print("Which card would you like to defend with? (enter 'x' to use no defense card)")
			defenseValue = None
			while defenseValue == None:
				inp = input("?> ").strip().lower()
				if inp in defenseValues:
					defenseValue = int(inp)
					for idx, card in enumerate(self.defenseCards):
						if card.value == defenseValue:
							self.defenseCards.pop(idx)
							self.game.deck.cards.append(card)
							break
					else:
						assert False
				elif inp == "x":
					defenseValue = 0
				elif inp != "":
					try:
						int(inp)
						print("You don't have that card.")
					except:
						print("Invalid input.")
			if defenseValue == 0:
				print("You will not use a defense card.")
			else:
				print("You are defending with a value of", str(defenseValue) + ".")
		input("Press enter to continue.")
		clearScreen()
		self.game.completeAttack(attacker, self, attackValue, defenseValue)

	def doStealCardsFromPlayer(self, victim, numCards):
		numCardsChosen = 0
		numStealableCards = victim.getNumStealableCards()
		if numStealableCards < numCards:
			pluralized = None
			if numStealableCards > 1:
				pluralized = "cards"
			else:
				pluralized = "card"
			print(victim.name, "only has", numStealableCards, pluralized, "so", self.name, "only gets to steal", numStealableCards, pluralized + ".")
			numCards = numStealableCards
		while numCards > 0:
			if numCards == 1 and numCardsChosen == 0:
				print(self.name + ", choose the suit of your card:")
			elif numCardsChosen == 0:
				print(self.name + ", choose the suit of your first card:")
			elif numCardsChosen == 1:
				print(self.name + ", choose the suit of your second card:")
			elif numCardsChosen == 2:
				print(self.name + ", choose the suit of your third card:")
			print("1. CLUBS (attack)")
			print("2. DIAMONDS (defense)")
			suit = None
			while suit == None:
				inp = input("?> ").strip().lower()
				if inp == "1":
					if len(victim.attackCards) == 0:
						print(victim.name, "has no attack cards. Choose another suit.")
					else:
						suit = "CLUBS"
				elif inp == "2":
					if len(victim.defenseCards) == 0:
						print(victim.name, "has no defense cards. Choose another suit.")
					else:
						suit = "DIAMONDS"
				elif inp != "":
					print("Invalid input.")

			possibleCards = None
			possibleValues = None
			if suit == "CLUBS":
				possibleCards = victim.attackCards
				possibleValues = [str(card.value) for card in possibleCards]
				print(victim.name, "has these attack cards:", ", ".join(possibleValues))
			elif suit == "DIAMONDS":
				possibleCards = victim.defenseCards
				possibleValues = [str(card.value) for card in possibleCards]
				print(victim.name, "has these defense cards:", ", ".join(possibleValues))

			print("Which value will you steal?")
			value = None
			while value == None:
				inp = input("?> ").strip().lower()
				if inp in possibleValues:
					value = int(inp)
				elif inp != "":
					try:
						int(inp)
						print(victim.name, "doesn't have that card.")
					except:
						print("Invalid input.")

			if suit == "CLUBS":
				for idx, crd in enumerate(victim.attackCards):
					if crd.value == value:
						card = victim.attackCards.pop(idx)
						self.attackCards.append(card)
						break
				else:
					assert False
			elif suit == "DIAMONDS":
				for idx, crd in enumerate(victim.defenseCards):
					if crd.value == value:
						card = victim.defenseCards.pop(idx)
						self.defenseCards.append(card)
						break
				else:
					assert False
			print(self.name, "stole the", value, "of", suit, "from", victim.name)
			victim.printStealableCards()
			print()

			numCards -= 1
			numCardsChosen += 1


game = Game()
game.startGame()






# ! start game
# ! start add players phase
#   clear screen
# @ "Add at least 2 players."
# < get players
#   if no players have been added
#     @ "\tNo players have been added."
#   else
#     @ "\tPlayers: " + <the players' names>
#   @ "\tPick an action."
#   @ "\t1. Add a new player."
#   @ "\t2. Finish adding players."




#add players phase
#	print initial info
#	get input
#		1. Add a new player
#			enter player name
#			continue
#		2. Start game
#			if there are 2 players
#				start setup phase
#			else
#				continue
#	print end info
#setup phase
#	print initial info
#	for player in players
#		*draw player initial cards
#	print end info
#	start game phase
#game phase
#	print initial info
#	for player in players sorted by speed
#		*1. Attack a player
#			*choose which card to use
#			*choose which player to attack
#			switch control to the other player
#				choose which defense card to use
#			
#		2. Draw a card
#			draw player card
#		3. Finish turn
#			continue
