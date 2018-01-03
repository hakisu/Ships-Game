package com.shipsgame.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.shipsgame.screens.ShipsGame;

public class DesktopLauncher {
    public static void main(String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.setFromDisplayMode(LwjglApplicationConfiguration.getDesktopDisplayMode());
        config.resizable = false;
        config.backgroundFPS = 60;
        config.foregroundFPS = 60;
        config.vSyncEnabled = true;
        config.width = 640;
        config.height = 640;
        config.fullscreen = false;

        new LwjglApplication(new ShipsGame(), config);
    }
}
//
//	Specyfikacja projektu:
//
//		Zaprojektuj i napisz asystenta gry w statki.
//
//		„Okręty” („Statki”) należą do gatunku gier strategicznych polegających na zatopieniu floty przeciwnika nie znając dokładnego jej położenia. Ze względu na swą prostotę, może ona być prowadzona z użyciem kartki i ołówka. Napisz program realizujący funkcje gry przy użyciu komputera.
//
//		Flota to:
//
//		czteromasztowiec x 1
//		trójmasztowce x 2
//		dwumasztowce x 3
//		jednomasztowce x 4
//		Plansza ma wymiary 10 x 10, kolumny mają oznaczenia literowe: A-J a wiersze liczbowe: 1-10
//
//		Asystent ma pozwolić na utworzenie profilu gracza, zabezpieczonego hasłem (przechowywanym w pliku). Po zalogowaniu użytkownika ma udostępnić mu menu z kilkoma opcjami: Nowa gra, Wróć do rozgrywki, Obejrzyj poprzednie gry, Profil użytkownika.
//
//		Opcja "Nowa gra", ma przeprowadzić użytkownika przez ustawienie floty na swojej planszy odpytując o położenie jednego końcowego wierzchołka kolejnych statków i wyświetlając warianty możliwych ich ułożeń do wyboru (poza jednomasztowcami).
//
//		Dodatkowo asystent w trybie rozgrywki pozwala na przechowywanie informacji o już wykonanych ruchach i pozwala na zaznaczenie trafień i zatopień.
//        Grę w statki rozbudować o zapis do pliku kolejnych kroków użytkownika. Pozwolić na uruchomienie programu z argumentem w postaci nazwy pliku, żeby można było ponownie obejrzeć przebieg gry.
//
//        Opcja "Wróć do rozgrywki" - pozwala wczytać ostatnią niedokończoną rozgrywkę.
//
//        Opcja "Obejrzyj ponownie gry - pozwala wczytać rozgrywki z danym przeciwnikiem z możliwością określenia daty jej wystąpienia (kolejne rozgrywki z danego dnia są opatrzone kolejnymi numerami
//
//        Opcja "Profil użytkownika" - pozwala na zmianę danych użytkownika i obejrzenie statystyk z gry