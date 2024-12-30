package com.mycompany.yazlab13;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class OncelikHesapla {

    // Müşteri sınıfı (iç içe sınıf olarak tanımlandı)
    public static class Musteri {
        private int id;
        private String tur; // "Premium" veya "Normal"
        private long beklemeSuresi; // Saniye cinsinden
        private double oncelikSkoru; // Dinamik olarak hesaplanacak

        public Musteri(int id, String tur, long beklemeSuresi) {
            this.id = id;
            this.tur = tur;
            this.beklemeSuresi = beklemeSuresi;
            this.oncelikSkoru = 0.0; // Başlangıçta 0
        }

        // Getter ve Setter'lar
        public int getId() {
            return id;
        }

        public String getTur() {
            return tur;
        }

        public void setTur(String tur) {
            this.tur = tur;
        }

        public long getBeklemeSuresi() {
            return beklemeSuresi;
        }

        public void setBeklemeSuresi(long beklemeSuresi) {
            this.beklemeSuresi = beklemeSuresi;
        }

        public double getOncelikSkoru() {
            return oncelikSkoru;
        }

        public void setOncelikSkoru(double oncelikSkoru) {
            this.oncelikSkoru = oncelikSkoru;
        }
// OncelikHesapla sınıfına eklenecek
       
        @Override
        public String toString() {
            return "Müşteri ID: " + id + ", Tür: " + tur + ", Bekleme Süresi: " + beklemeSuresi + " sn, Öncelik Skoru: " + oncelikSkoru;
        }
    }

    private List<Musteri> musteriler; // Müşteri listesi

    // Constructor
    public OncelikHesapla() {
        musteriler = new ArrayList<>();
    }
 public List<Musteri> getMusteriler() {
            return musteriler;
}
    // Müşteri ekleme metodu
    public void musteriEkle(int id, String tur, long beklemeSuresi) {
        Musteri yeniMusteri = new Musteri(id, tur, beklemeSuresi);
        musteriler.add(yeniMusteri);
    }

    // Öncelik skoru hesaplama
    public void oncelikHesapla() {
        double beklemeSureAgirligi = 0.5;

        for (Musteri musteri : musteriler) {
            double temelOncelikSkoru = musteri.getTur().equalsIgnoreCase("Premium") ? 15 : 10;
            double yeniSkor = temelOncelikSkoru + (musteri.getBeklemeSuresi() * beklemeSureAgirligi);
            musteri.setOncelikSkoru(yeniSkor);
        }
    }

    // Müşterileri öncelik sırasına göre güncelleme
    public void oncelikSirala() {
        musteriler.sort(Comparator.comparingDouble(Musteri::getOncelikSkoru).reversed());
    }

    // Listeyi yazdırma
    public void listeyiYazdir() {
        for (Musteri musteri : musteriler) {
            System.out.println(musteri);
        }
    }

    // Bekleme süresi güncelleme (örnek için)
    public void beklemeSuresiGuncelle(int musteriId, long yeniBeklemeSuresi) {
        for (Musteri musteri : musteriler) {
            if (musteri.getId() == musteriId) {
                musteri.setBeklemeSuresi(yeniBeklemeSuresi);
                break;
            }
        }
    }
}
