package com.example.myapp;



import java.util.*;

class Player {
    String name;
    int hp;
    int attack;
    int potions;

    Player(String name) {
        this.name = name;
        this.hp = 100;
        this.attack = 15;
        this.potions = 3;
    }

    void heal() {
        if (potions > 0) {
            hp += 25;
            potions--;
            if (hp > 100) hp = 100;
            System.out.println("🧪 Potion utilisée. HP = " + hp);
        } else {
            System.out.println("❌ Plus de potions !");
        }
    }
}

class Enemy {
    String type;
    int hp;
    int attack;

    Enemy(String type, int hp, int attack) {
        this.type = type;
        this.hp = hp;
        this.attack = attack;
    }
}

public class Main {

    static Scanner scanner = new Scanner(System.in);
    static Random random = new Random();

    public static void main(String[] args) {

        System.out.println("⚔️ Bienvenue dans Dungeon Quest !");
        System.out.print("Entre ton nom : ");
        String name = scanner.nextLine();

        Player player = new Player(name);
        System.out.println("👤 Héros créé : " + player.name);

        while (player.hp > 0) {
            Enemy enemy = generateEnemy();
            System.out.println("\n👹 Un " + enemy.type + " apparaît !");

            while (enemy.hp > 0 && player.hp > 0) {
                System.out.println("\n🧠 Choix :");
                System.out.println("1. Attaquer");
                System.out.println("2. Utiliser potion");
                System.out.println("3. Fuir");

                int choice = scanner.nextInt();

                switch (choice) {
                    case 1:
                        int dmg = player.attack + random.nextInt(6);
                        enemy.hp -= dmg;
                        System.out.println("⚔️ Tu infliges " + dmg + " dégâts.");
                        break;

                    case 2:
                        player.heal();
                        break;

                    case 3:
                        System.out.println("🏃 Tu fuis le combat !");
                        enemy.hp = 0;
                        continue;

                    default:
                        System.out.println("❓ Choix invalide");
                }

                if (enemy.hp > 0) {
                    int enemyDmg = enemy.attack + random.nextInt(5);
                    player.hp -= enemyDmg;
                    System.out.println("💥 L'ennemi t'inflige " + enemyDmg + " dégâts.");
                }
            }

            if (player.hp <= 0) {
                System.out.println("\n💀 GAME OVER");
                break;
            } else {
                System.out.println("✅ Ennemi vaincu !");
            }
        }

        scanner.close();
    }

    static Enemy generateEnemy() {
        String[] types = {"Gobelin", "Squelette", "Orc", "Démon"};
        String type = types[random.nextInt(types.length)];
        int hp = 30 + random.nextInt(40);
        int attack = 8 + random.nextInt(6);
        return new Enemy(type, hp, attack);
    }
}