# FinanceStep

---

<img width="1046" height="190" alt="FinanceStep (logo)" src="https://github.com/user-attachments/assets/c7ff6a52-fbeb-46ef-839c-a7c62c68ae60" />

A modern JavaFX application to manage personal finance, tasks, and allowance requests for families.

---

# About the project

**FinanceStep** is an intuitive JavaFX desktop application designed to simplify personal financial management and introduce young users to financial literacy within a family context. 

Managing personal expenses, allowance, and daily chores can often become chaotic. FinanceStep solves this by providing a unified, user-friendly platform where users can track their spending, manage savings goals, and monitor tasks or financial requests in real time.

### Key Features

* 💰 **Wallet & Piggy Bank Management** 
	* Real-time tracking of current wallet balance. 
	* Dedicated "Piggy Bank" (*Salvadanaio*) with deposit and withdrawal capabilities. 
	* Customizable savings target (*Obiettivo*) with instant progress tracking. 
* 📊 **Transaction Management** 
	* Clear overview of all financial transactions with sorting and details (date, description, type, amount). 
	* Filter and edit capabilities for past transactions. 
* 📋 **Task & Chore System** 
	* Assign tasks (*Compiti*) with associated monetary rewards and deadlines. 
	* Real-time status updates and dynamic notification badges to highlight pending tasks. 
* 📩 **Extra Funds Requests** 
	* Dedicated system for submitting and reviewing extra allowance requests (*Richieste Extra*).
	* Clear status indicators for approval workflows. 
* 👁️ **Junior Monitoring Mode** 
	* Specialized view allowing parents/guardians to monitor transaction histories and progress for junior accounts.

---
# Preview

The Auth Page is the first windows you will see when you run the project.
Here you can choose to **Sign Up** (*Registrazione*) or **Sign In** (*Accedi*).

<img width="127" height="155" alt="Screenshot 2026-07-27 033758" src="https://github.com/user-attachments/assets/513e6745-92e2-405a-9cfa-d83006fabb64" />

If you choose **Sign Up**, you will see this window: create a new account by putting your *username*, *password* and set your role (*Tutor*, if you are a parent or a major relative, or *Junior*, if you are a kid).

<img width="176" height="259" alt="Screenshot 2026-07-27 033811" src="https://github.com/user-attachments/assets/8d685837-87b6-4460-a415-6d5776d50e07" />

If you choose **Sign In**, your account already exist and you must insert only your username and password.

<img width="170" height="214" alt="Screenshot 2026-07-27 033854" src="https://github.com/user-attachments/assets/c3943167-ea1f-4940-a1a0-530ef450827f" />

I made it to make the app more realistic and safer but there aren't functions to recover your account if you don't remember your credentials.

Once you have entered the credentials, you will see the main page of the application.

Example of a **Junior User**:

<img width="959" height="502" alt="Screenshot 2026-07-27 033909" src="https://github.com/user-attachments/assets/e12f1778-2896-4dfb-aaff-3ae4d83e32ad" />


- At the top there is the *Menu bar* for the various features. 
- In the center, you will find the balance of your wallet (*Portafoglio*) and your piggy bank for saving money (*Salvadanaio*). 
- Below there is a table containing all transactions, tasks, and requests.

To create a new transaction, play the button (*Nuova Transazione*) below *Saldo Portafoglio*.
Enter the requested informations.
A Transaction (*Transazione*) can be of two types:
- Incoming (*Entrata*) : moneys arrive in your wallet or piggy bank.
- Outlay (*Spesa*) : moneys come out of your wallet or piggy bank.

<img width="451" height="133" alt="Screenshot 2026-07-27 034013" src="https://github.com/user-attachments/assets/8010379a-f0f8-4283-bda3-d3f2ddc0112e" />


Below *Salvadanaio* there are two buttons:

1) "*Versa*" : it adds money to the piggy bank from his wallet.
	1) <img width="269" height="139" alt="Screenshot 2026-07-27 034141" src="https://github.com/user-attachments/assets/da6758c7-0fff-489d-baf8-50169264742b" />

2) "*Preleva*" : it takes the money out of the piggy bank and puts it in his wallet.
	1) <img width="271" height="140" alt="Screenshot 2026-07-27 034153" src="https://github.com/user-attachments/assets/47bfb0f0-b06e-4cde-9212-fbd8fee8bf69" />


Near *Salvadanaio* you can also set a savings goal (maybe in future you want to buy something too expensive).

<img width="271" height="140" alt="Screenshot 2026-07-27 034206" src="https://github.com/user-attachments/assets/3518cb0a-e2be-46e5-92ee-225aa435745a" />


You can see this three buttons:
1) *Transazioni*
2) *Compiti*
3) *Richieste Extra*

In the first case, you can see a table which shows you the list of all your transactions.

<img width="949" height="317" alt="Screenshot 2026-07-27 034224" src="https://github.com/user-attachments/assets/2f6f383b-546f-4e27-ac82-92a443ccd60f" />


In the second case, you can see a table which shows:
- If you are a Junior user the list of tasks to do.
	- <img width="948" height="322" alt="Screenshot 2026-07-27 034250" src="https://github.com/user-attachments/assets/360ea1bd-a6d9-464a-8506-4071c28fb6bd" />

- If you are a Tutor user the list of task created (by play the button *Nuovo Compito*) for a specific Junior user. 
	- <img width="950" height="341" alt="Screenshot 2026-07-27 042328" src="https://github.com/user-attachments/assets/9050a559-f366-4c5f-a209-daf0d3f362ca" />

	- <img width="242" height="298" alt="Screenshot 2026-07-27 042346" src="https://github.com/user-attachments/assets/29f37827-ceae-4ea4-a963-7b746ca2e0e2" />


In the third case, you can see a table which shows you:
- If you are a Tutor user the list of requests to accept or reject.
	- <img width="950" height="332" alt="Screenshot 2026-07-27 042534" src="https://github.com/user-attachments/assets/7b773255-45f5-4ccb-a251-47a6d9031713" />

- If you are a Junior user the list of requests made for a specific Tutor user (by play the button *Nuova Richiesta*).
	- <img width="952" height="344" alt="Screenshot 2026-07-27 043408" src="https://github.com/user-attachments/assets/9007aff0-78e1-4b05-a5f5-46c0955cc785" />

	- <img width="239" height="281" alt="Screenshot 2026-07-27 043420" src="https://github.com/user-attachments/assets/14de5d5f-4a63-485c-8a08-81d39aea71c8" />


Below the table if you are a Tutor user, you can see a button named "*Monitora Transazioni di uno Junior*", which means that you can see all the list of the transactions of a specific Junior user.

<img width="955" height="344" alt="Screenshot 2026-07-27 043117" src="https://github.com/user-attachments/assets/996e1311-5ffa-44b4-bf37-be88010022a9" />


<img width="330" height="98" alt="Screenshot 2026-07-27 043133" src="https://github.com/user-attachments/assets/8521629f-d0d1-4b71-b462-32840ca70056" />


<img width="447" height="320" alt="Screenshot 2026-07-27 043243" src="https://github.com/user-attachments/assets/ddf0f3cb-2065-44ac-8caa-2c0d763a040f" />


---
# Requirements

Make sure you have the following installed on your computer: 
- **Java Development Kit (JDK)**: Version 17 or higher (JDK 19 recommended) 
- **Maven**: Version 3.6+ (if you want to build/run via CLI) 
- **Git**: To clone the repository

---
# How to use the application

You can run this application, here's how:
- Go to this link https://github.com/laurartt02/FinanceStep
- Click the green button Code on the top right of the screen and copy the HTTPS link
- On Intellij go to File -> New -> Project from Version Control...
- Copy the previous link in the URL field and press Clone

---
# Author and Credits

- **Name** : Laura Ratti (@laurartt02)
- **University** : Computer Engineering - UniMoRe
- **Exam** : Object-Oriented Programming (from Nicola Bicocchi)


