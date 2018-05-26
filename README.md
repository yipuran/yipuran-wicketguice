# yipuran-wicketguice
Wicket Ioc with Google guice Library
**このライブラリは、古いWicket, Java8時代に作成したもので、使用すべきでない**
**これはつまり、GuiceComponentInjectorの代用で作られたCustomGuiceComponentInjectorを廃止することである**

###### yipuran-wicketguice を使う場合の WebApplitionクラス
```java
public class MyApplication extends WebApplication{
   @Override
   protected void init(){
      getComponentInstantiationListeners().add(new CustomGuiceComponentInjector(this, new AbstractModule(){
         @Override
         protected void configure(){

         }
      });
```
###### yipuran-wicketguice を使用しない。yipuran-wicketguice を使わない。
```java
public class MyApplication extends WebApplication{
   @Override
   protected void init(){
      getComponentInstantiationListeners().add(new GuiceComponentInjector(this, new AbstractModule(){
         @Override
         protected void configure(){

         }
      });
```
なぜ、これを github に置いているか？→ 古いコードに対応するため。

## Dependency
Wicket


## Setup pom.xml
```
<repositories>
   <repository>
      <id>yipuran-wicketguice</id>
      <url>https://raw.github.com/yipuran/yipuran-wicketguice/mvn-repo</url>
   </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>org.yipuran.wicketguice</groupId>
        <artifactId>yipuran-wicketguice</artifactId>
        <version>4.0</version>
    </dependency>
</dependencies>
```
