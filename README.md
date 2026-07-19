# DupersUnited Mod

A Minecraft mod to help debug plugins and servers. This Mod does **NOT** give you dupes on Minecraft servers, just helps in finding them.

## Installation

1. Download the latest release from the [releases](https://github.com/DupersUnited/dupersunited-mod/releases) page
2. Place the `.jar` file in your `mods` folder
3. Launch Minecraft with Fabric

## Building

Clone the repository and navigate into it:

```bash
git clone https://github.com/DupersUnited/dupersunited-mod.git
cd dupersunited-mod
```

Build the project:

```
./gradlew build
```

The compiled mod will be located in:

```
build/libs/
```

# How to use this mod

### Linking proxies to accounts
Once you first install the mod, and exit the welcome screen, you'll see a button in the top right saying "DupersUnited",
<img width="1357" height="709" alt="Screenshot 2026-04-29 210225" src="https://github.com/user-attachments/assets/47728a5d-8f2f-44a2-bd7c-d9e79cacbae5" />
after clicking on the "DupersUnited" button, it will open this screen. You can also access this screen via the Multiplayer menu <img width="1353" height="708" alt="Screenshot 2026-04-29 211927" src="https://github.com/user-attachments/assets/934bfd66-9589-44c5-8a5f-7667f7b8b967" />
<img width="1919" height="1012" alt="Screenshot 2026-04-29 210332" src="https://github.com/user-attachments/assets/2acf762f-a814-4944-aac9-70f9c7215a53" />
Now to link an account to a proxy, you'll first have to create a proxy, click on the "Proxy Manager" button and create one, once you do that go back to the config screen and click on the "Accounts" button.
<img width="1918" height="224" alt="Screenshot 2026-04-29 211029" src="https://github.com/user-attachments/assets/636aa19f-b1de-42a9-8e9e-df70d6cbfd6e" />
click on the "Proxy" button next to the account you want to link
<img width="1919" height="168" alt="Screenshot 2026-04-29 211105" src="https://github.com/user-attachments/assets/9c7d3e13-3a3a-43e1-bbaf-44397f4f5d56" />
then go back to "Proxy Manager" and make sure Proxies are **ENABLED** for it to work, now everytime you launch with the account or swap to it using our account menu it will automatically swap you to that proxy.

# What do all the buttons in the Multiplayer screen do?

### Server Alerts

Server Alerts lists known servers who use exploits to track users & servers who **ETHICALLY** monetize their servers (Non P2W/Gambling), use [ExploitPreventer](https://github.com/NikOverflow/ExploitPreventer) or [OpSec](https://github.com/aurickk/OpSec) to be fully safe from those exploits. All Server Alerts does is puts a warning screen before logging into a server that uses an exploit
<img width="923" height="289" alt="Screenshot 2026-04-29 213938" src="https://github.com/user-attachments/assets/27163370-c4ed-406a-a79f-275713cf3575" />

### RP Bypass
Bypasses required resource pack on a servers
### Brand Spoof
Spoofs your brand to a Vanilla Client
## In-Game
By default you will have inventory buttons, open your inventory or any container to see them

<img width="1919" height="1002" alt="Screenshot 2026-04-29 202928" src="https://github.com/user-attachments/assets/82c772ed-ec61-44fc-9629-5638ffa1c85b" />

### What does each inventory button do?
- "Close Without Packet" closes your current GUI without sending a packet to the server. (To restore press your V key)
- "Clear GUI Cache" will clear all your currently saved GUIs
- "DC & Send Packets" sends all currently queued packets (if you have any) and disconnects you from the server.
- "Delay Packets" will only pause **GUI** related packets.
- "Save GUI" saves your current GUI without closing it
- "Chat or command" allows you to type commands while inside a container.
- "Fabricate Packet" allows you to create a custom ClickSlotC2SPacket and ButtonClickC2SPacket within a window it creates.
- "Sync ID" number that makes sure the game knows which screen or menu the data is for..
- "Revision" number that increases every time something changes, so the game knows it has the newest version.
- "Copy GUI as JSON" copies GUI NBT as a JSON

To access the other modules in the mod, press your "K" key while ingame.

<img width="1919" height="1008" alt="Screenshot 2026-04-29 204532" src="https://github.com/user-attachments/assets/01394a08-36f7-4cb9-b858-0a0e4ea21e62" />

To access the commands run "/du help"
<img width="566" height="338" alt="Screenshot 2026-04-29 205014" src="https://github.com/user-attachments/assets/d338eee3-fb0e-4051-89a7-2657a82e3817" />
