package com.davtyan.mytaxi.model;

public class User {
   private String email;
   private String password;
   private String name;

   public User(String email, String password, String name) {
      this.email = email;
      this.password = password;
      this.name = name;
   }

   public static class Code {
      private String code;

      public Code(String code) {
         this.code = code;
      }
   }
}
