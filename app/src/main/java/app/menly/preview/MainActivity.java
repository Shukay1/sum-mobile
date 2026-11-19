package app.menly.preview;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.text.InputType;
import java.util.*;
import org.json.*;
import android.widget.ImageView;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import java.io.InputStream;

public class MainActivity extends Activity {
  final int BG=Color.rgb(9,9,13), CARD=Color.rgb(22,20,27), LINE=Color.rgb(43,39,47), WHITE=Color.WHITE, MUTED=Color.rgb(151,145,156);
  int accent=Color.rgb(184,61,255), page=0; boolean anon=false; String selectedMedia=""; String audience="Вся Центральная Азия", chatMode="Личные";
  LinearLayout root,content,nav; ScrollView scroll;
  SharedPreferences prefs;
  ArrayList<Post> posts=new ArrayList<>();
  ArrayList<String> groups=new ArrayList<>(Arrays.asList("Парни Бишкека","Друзья Алматы"));
  HashMap<String,ArrayList<String>> messages=new HashMap<>();
  static class Post {
    String author,time,text,media=""; long created=System.currentTimeMillis(); int likes,comments,reposts; boolean liked,anonymous,photos;
    Post(String a,String t,String s,int l,int c,int r,boolean an,boolean ph){author=a;time=t;text=s;likes=l;comments=c;reposts=r;anonymous=an;photos=ph;}
  }
  int dp(float x){return (int)(x*getResources().getDisplayMetrics().density+.5f);}
  GradientDrawable shape(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
  TextView text(String s,int size,int color,boolean bold){TextView v=new TextView(this);v.setText(s);v.setTextSize(size);v.setTextColor(color);v.setGravity(Gravity.CENTER_VERTICAL);if(bold)v.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return v;}
  LinearLayout vertical(){LinearLayout l=new LinearLayout(this);l.setOrientation(1);return l;}
  LinearLayout horizontal(){LinearLayout l=new LinearLayout(this);l.setOrientation(0);l.setGravity(Gravity.CENTER_VERTICAL);return l;}
  LinearLayout.LayoutParams lp(int w,int h){return new LinearLayout.LayoutParams(w<0?w:dp(w),h<0?h:dp(h));}
  void pad(View v,int a,int b,int c,int d){v.setPadding(dp(a),dp(b),dp(c),dp(d));}
  void line(LinearLayout l){View v=new View(this);v.setBackgroundColor(LINE);l.addView(v,lp(-1,1));}
  TextView button(String s,Runnable r){TextView v=text(s,15,WHITE,false);v.setGravity(Gravity.CENTER);v.setBackground(shape(CARD,13));pad(v,14,10,14,10);v.setOnClickListener(w->r.run());return v;}
  void heading(String s){TextView h=text(s,24,WHITE,true);pad(h,18,21,18,18);content.addView(h,lp(-1,-2));}
  void row(String title,String sub,Runnable r){
    LinearLayout a=horizontal();pad(a,18,15,18,15);LinearLayout b=vertical();b.addView(text(title,15,WHITE,true));if(sub!=null){TextView c=text(sub,12,MUTED,false);pad(c,0,5,0,0);b.addView(c);}a.addView(b,new LinearLayout.LayoutParams(0,-2,1));a.addView(text("›",23,MUTED,false));content.addView(a);line(content);a.setOnClickListener(v->r.run());
  }
  @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(BG);getWindow().setNavigationBarColor(BG);prefs=getSharedPreferences("menly",MODE_PRIVATE);accent=prefs.getInt("accent",accent);
    posts.add(new Post("Анонимный пользователь","2 ч","Кто сегодня в Бишкеке свободен? Хочется приятной компании вечером 🥺",128,34,12,true,false));
    posts.add(new Post("azamat.kg","4 ч","Погода сегодня просто идеальная для прогулки по городу 🌿",256,18,7,false,true));
    posts.add(new Post("nurlan.u","6 ч","Ищу новых друзей в Алматы. Пишите, пообщаемся! 💗",92,11,3,false,false));
    restore();show(0);
  }
  void persist(){
    try{
      JSONArray arr=new JSONArray();
      for(Post p:posts){JSONObject o=new JSONObject();o.put("author",p.author);o.put("time",p.time);o.put("text",p.text);o.put("likes",p.likes);o.put("comments",p.comments);o.put("reposts",p.reposts);o.put("anonymous",p.anonymous);o.put("photos",p.photos);o.put("liked",p.liked);o.put("created",p.created);o.put("media",p.media);arr.put(o);}
      prefs.edit().putString("posts",arr.toString()).putString("groups",new JSONArray(groups).toString()).apply();
    }catch(Exception e){}
  }
  void restore(){
    try{
      String data=prefs.getString("posts",null);
      if(data!=null){JSONArray arr=new JSONArray(data);posts.clear();for(int i=0;i<arr.length();i++){JSONObject o=arr.getJSONObject(i);Post p=new Post(o.getString("author"),o.getString("time"),o.getString("text"),o.getInt("likes"),o.getInt("comments"),o.getInt("reposts"),o.getBoolean("anonymous"),o.getBoolean("photos"));p.liked=o.optBoolean("liked");p.created=o.optLong("created",System.currentTimeMillis());p.media=o.optString("media","");posts.add(p);}}
      JSONArray g=new JSONArray(prefs.getString("groups","[]"));if(g.length()>0){groups.clear();for(int i=0;i<g.length();i++)groups.add(g.getString(i));}
      for(int i=posts.size()-1;i>=0;i--){Post p=posts.get(i);if(p.anonymous&&System.currentTimeMillis()-p.created>=86400000L)posts.remove(i);}
      persist();
    }catch(Exception e){}
  }
  void show(int index){
    page=index;root=vertical();root.setBackgroundColor(BG);setContentView(root);
    scroll=new ScrollView(this);scroll.setFillViewport(true);content=vertical();scroll.addView(content);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
    if(index==0)feed();if(index==1)chats();if(index==2)create();if(index==3)activity();if(index==4)profile();bottom();
  }
  void bottom(){
    line(root);nav=horizontal();nav.setBackgroundColor(BG);
    String[] icons={"⌂","♧","⊞","♡","♙"};
    for(int i=0;i<5;i++){final int n=i;TextView b=text(icons[i],29,i==page?accent:MUTED,false);b.setGravity(Gravity.CENTER);nav.addView(b,new LinearLayout.LayoutParams(0,dp(62),1));b.setOnClickListener(v->show(n));}
    root.addView(nav);
  }
  void feed(){
    LinearLayout bar=horizontal();pad(bar,20,12,20,8);TextView logo=text("♡",34,accent,true);bar.addView(logo,new LinearLayout.LayoutParams(0,dp(46),1));TextView bell=button("♧",()->notifications());bar.addView(bell);content.addView(bar);
    tabs(new String[]{"Для тебя","Подписки"},0,i->{if(i==1)dialog("Подписки","Подписки будут синхронизироваться после подключения сервера");});
    stories();
    for(int i=0;i<posts.size();i++)postCard(i);
  }
  void stories(){
    HorizontalScrollView sc=new HorizontalScrollView(this);sc.setHorizontalScrollBarEnabled(false);
    LinearLayout strip=horizontal();pad(strip,14,15,14,15);
    String[] names={"Твои истории","Азамат","Бек","Эмир","Руслан"};
    String[] pics={"avatar_profile.jpg","avatar_azamat.jpg","avatar_nurlan.jpg","avatar_emir.jpg","avatar_profile.jpg"};
    for(int i=0;i<names.length;i++){
      final int n=i;LinearLayout item=vertical();item.setGravity(Gravity.CENTER);
      ImageView avatar=photo(pics[i],100);LinearLayout.LayoutParams ap=lp(62,62);ap.gravity=Gravity.CENTER;item.addView(avatar,ap);
      TextView label=text(names[i],11,WHITE,false);label.setGravity(Gravity.CENTER);pad(label,2,7,2,0);item.addView(label);
      LinearLayout.LayoutParams it=lp(78,-2);it.setMargins(dp(2),0,dp(2),0);strip.addView(item,it);
      item.setOnClickListener(v->{if(n==0)chooseStory();else dialog("История · "+names[n],"Истории пользователей появятся после подключения сервера");});
    }
    sc.addView(strip);content.addView(sc);line(content);
  }
  void chooseStory(){android.content.Intent in=new android.content.Intent(android.content.Intent.ACTION_OPEN_DOCUMENT);in.addCategory(android.content.Intent.CATEGORY_OPENABLE);in.setType("image/*");startActivityForResult(in,402);}
  interface Select {void choose(int index);}
  void tabs(String[] names,int selected,Select onSelect){
    LinearLayout tabs=horizontal();for(int i=0;i<names.length;i++){final int k=i;LinearLayout cell=vertical();TextView t=text(names[i],14,i==selected?WHITE:MUTED,i==selected);t.setGravity(Gravity.CENTER);cell.addView(t,lp(-1,45));View under=new View(this);under.setBackgroundColor(i==selected?accent:BG);cell.addView(under,lp(-1,3));tabs.addView(cell,new LinearLayout.LayoutParams(0,-2,1));cell.setOnClickListener(v->onSelect.choose(k));}content.addView(tabs);line(content);
  }
  ImageView photo(String asset,int radius){
    ImageView img=new ImageView(this);
    img.setScaleType(ImageView.ScaleType.CENTER_CROP);
    try(InputStream stream=getAssets().open(asset)){
      Bitmap bitmap=BitmapFactory.decodeStream(stream);
      if(bitmap!=null)img.setImageBitmap(bitmap);
    }catch(Exception e){img.setBackground(shape(CARD,radius));}
    img.setClipToOutline(true);
    img.setBackground(shape(CARD,radius));
    return img;
  }
  void postCard(int i){
    Post p=posts.get(i);LinearLayout card=vertical();pad(card,15,15,15,15);
    LinearLayout top=horizontal();if(p.anonymous){TextView avatar=text("♡",24,WHITE,true);avatar.setGravity(Gravity.CENTER);avatar.setBackground(shape(accent,50));top.addView(avatar,lp(43,43));}
    else{ImageView avatar=photo(p.author.equals("nurlan.u")?"avatar_nurlan.jpg":"avatar_azamat.jpg",50);top.addView(avatar,lp(43,43));}
    LinearLayout info=vertical();pad(info,11,0,0,0);info.addView(text(p.author,14,WHITE,true));info.addView(text("· "+p.time,12,MUTED,false));top.addView(info,new LinearLayout.LayoutParams(0,-2,1));
    TextView more=text("⋯",25,MUTED,false);top.addView(more);more.setOnClickListener(v->options("Публикация",new String[]{"Скрыть","Пожаловаться"},k->dialog("Публикация","Действие будет доступно после подключения сервера")));
    card.addView(top);TextView body=text(p.text,15,WHITE,false);body.setLineSpacing(dp(4),1);pad(body,54,10,2,12);card.addView(body);
    if(p.anonymous){TextView tags=text("#Бишкек     #Встреча     #Сегодня",12,Color.rgb(223,145,179),false);pad(tags,54,0,0,13);card.addView(tags);}
    if(!p.media.isEmpty()){
      try{ImageView media=new ImageView(this);media.setScaleType(ImageView.ScaleType.CENTER_CROP);media.setImageURI(android.net.Uri.parse(p.media));media.setBackground(shape(CARD,12));LinearLayout.LayoutParams mp=lp(-1,230);mp.setMargins(dp(54),0,0,dp(10));card.addView(media,mp);}catch(Exception e){}
    }
    if(p.photos){LinearLayout photos=horizontal();pad(photos,54,0,0,10);ImageView a=photo("city.jpg",12),b=photo("wheel.jpg",12);LinearLayout.LayoutParams x=new LinearLayout.LayoutParams(0,dp(140),1);x.setMargins(0,0,dp(5),0);photos.addView(a,x);photos.addView(b,new LinearLayout.LayoutParams(0,dp(140),1));card.addView(photos);}
    LinearLayout actions=horizontal();pad(actions,54,0,0,0);
    String[] labels={(p.liked?"♥ ":"♡ ")+p.likes,"▢ "+p.comments,"⇄ "+p.reposts,"➤"};for(int k=0;k<4;k++){final int act=k;TextView btn=text(labels[k],13,k==0&&p.liked?accent:MUTED,false);btn.setGravity(Gravity.CENTER);actions.addView(btn,new LinearLayout.LayoutParams(0,dp(38),1));btn.setOnClickListener(v->{if(act==0){p.liked=!p.liked;p.likes+=p.liked?1:-1;persist();show(0);}if(act==1)comments(p);if(act==2){p.reposts++;persist();show(0);}if(act==3)dialog("Поделиться","Публикации пока доступны только внутри демонстрации");});}card.addView(actions);content.addView(card);line(content);
  }
  void comments(Post p){EditText input=new EditText(this);input.setHint("Написать комментарий");new AlertDialog.Builder(this).setTitle("Комментарии · "+p.comments).setView(input).setNegativeButton("Отмена",null).setPositiveButton("Отправить",(d,w)->{if(input.getText().length()>0){p.comments++;persist();show(0);}}).show();}
  void chats(){heading("Чаты");chatTabs();
    EditText search=new EditText(this);search.setSingleLine();search.setTextColor(WHITE);search.setHintTextColor(MUTED);search.setHint("Поиск");search.setBackground(shape(CARD,13));pad(search,15,9,15,9);LinearLayout.LayoutParams sp=lp(-1,48);sp.setMargins(dp(18),dp(15),dp(18),dp(10));content.addView(search,sp);
    if(chatMode.equals("Группы"))row("＋ Создать группу","Публичная или приватная",()->newGroup());
    String[] names=chatMode.equals("Группы")?groups.toArray(new String[0]):new String[]{"Азамат","Нурлан","Эмир"};
    for(String n:names)row(n,chatMode.equals("Группы")?"Групповой чат":"Нажми, чтобы открыть переписку",()->conversation(n));
  }
  void chatTabs(){
    LinearLayout bar=horizontal();pad(bar,16,4,16,8);
    String[] labels={"Личные","Группы"};
    for(int i=0;i<2;i++){final int k=i;boolean active=chatMode.equals(labels[i]);TextView tab=text(labels[i],14,active?WHITE:MUTED,active);tab.setGravity(Gravity.CENTER);tab.setBackground(shape(active?accent:CARD,18));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(40),1);p.setMargins(0,0,dp(7),0);bar.addView(tab,p);tab.setOnClickListener(v->{chatMode=labels[k];show(1);});}
    TextView req=button("Запросы  3",()->requests());bar.addView(req);content.addView(bar);line(content);
  }
  void requests(){new AlertDialog.Builder(this).setTitle("Запросы на переписку").setItems(new String[]{"Новый запрос · @aidar.kg","Новый запрос · @bek.kg","Новый запрос · @erlan.kg"},(d,w)->new AlertDialog.Builder(this).setTitle("Запрос").setMessage("Принять запрос на переписку?").setNegativeButton("Отклонить",null).setPositiveButton("Принять",(a,b)->conversation(new String[]{"Айдар","Бек","Эрлан"}[w])).show()).setNegativeButton("Закрыть",null).show();}
  void newGroup(){EditText input=new EditText(this);input.setHint("Название группы");new AlertDialog.Builder(this).setTitle("Новая группа").setView(input).setNegativeButton("Отмена",null).setPositiveButton("Создать",(d,w)->{String s=input.getText().toString().trim();if(!s.isEmpty()){groups.add(s);persist();show(1);}}).show();}
  void conversation(String name){
    root=vertical();root.setBackgroundColor(BG);setContentView(root);LinearLayout head=horizontal();TextView back=button("‹",()->show(1));head.addView(back);TextView title=text(name,19,WHITE,true);pad(title,15,0,0,0);head.addView(title);pad(head,10,12,0,12);root.addView(head);line(root);
    ScrollView sv=new ScrollView(this);LinearLayout msgs=vertical();pad(msgs,16,15,16,15);sv.addView(msgs);root.addView(sv,new LinearLayout.LayoutParams(-1,0,1));
    ArrayList<String> history=messages.computeIfAbsent(name,k->new ArrayList<>());
    if(history.isEmpty()){try{JSONArray saved=new JSONArray(prefs.getString("chat_"+name,"[]"));for(int j=0;j<saved.length();j++)history.add(saved.getString(j));}catch(Exception e){}}
    if(history.isEmpty())history.add("Привет! Как дела?");
    for(String m:history){TextView bubble=text(m,15,WHITE,false);pad(bubble,14,11,14,11);bubble.setBackground(shape(CARD,16));LinearLayout.LayoutParams b=lp(-2,-2);b.setMargins(0,0,0,dp(10));msgs.addView(bubble,b);}
    LinearLayout compose=horizontal();pad(compose,10,7,10,12);EditText entry=new EditText(this);entry.setSingleLine();entry.setTextColor(WHITE);entry.setHint("Сообщение");entry.setHintTextColor(MUTED);entry.setBackground(shape(CARD,22));pad(entry,13,9,13,9);compose.addView(entry,new LinearLayout.LayoutParams(0,dp(46),1));TextView send=button("➤",()->{String m=entry.getText().toString().trim();if(!m.isEmpty()){history.add(m);prefs.edit().putString("chat_"+name,new JSONArray(history).toString()).apply();conversation(name);}});compose.addView(send);root.addView(compose);
  }
  void create(){heading("Новая публикация");EditText field=new EditText(this);field.setId(881);field.setTextColor(WHITE);field.setHintTextColor(MUTED);field.setHint("О чём думаешь?");field.setGravity(Gravity.TOP);field.setMinLines(7);field.setBackground(shape(CARD,15));pad(field,16,15,16,15);LinearLayout.LayoutParams f=lp(-1,-2);f.setMargins(dp(18),0,dp(18),dp(10));content.addView(field,f);
    row("Анонимная публикация",anon?"Включено · исчезнет через 24 часа":"Выключено",()->{anon=!anon;String s=field.getText().toString();show(2);((EditText)findViewById(881)).setText(s);});
    row("Кто увидит публикацию",audience,()->options("Аудитория",new String[]{"Вся Центральная Азия","Моя страна","1 км","5 км","10 км","25 км","50 км","100 км"},i->{audience=new String[]{"Вся Центральная Азия","Моя страна","1 км","5 км","10 км","25 км","50 км","100 км"}[i];String s=field.getText().toString();show(2);((EditText)findViewById(881)).setText(s);}));
    row("Добавить фото или видео",selectedMedia.isEmpty()?"Выбрать из галереи":"Фото выбрано ✓",()->chooseMedia());
    TextView publish=button("Опубликовать",()->{String s=field.getText().toString().trim();if(s.isEmpty()){Toast.makeText(this,"Напиши текст",Toast.LENGTH_SHORT).show();return;}Post created=new Post(anon?"Анонимный пользователь":"menly_user","сейчас",s,0,0,0,anon,false);created.media=selectedMedia;posts.add(0,created);selectedMedia="";anon=false;persist();show(0);});publish.setBackground(shape(accent,14));LinearLayout.LayoutParams p=lp(-1,52);p.setMargins(dp(18),dp(20),dp(18),0);content.addView(publish,p);
  }
  void chooseMedia(){android.content.Intent intent=new android.content.Intent(android.content.Intent.ACTION_OPEN_DOCUMENT);intent.addCategory(android.content.Intent.CATEGORY_OPENABLE);intent.setType("image/*");intent.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION|android.content.Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);startActivityForResult(intent,400);}
  @Override protected void onActivityResult(int requestCode,int resultCode,android.content.Intent data){super.onActivityResult(requestCode,resultCode,data);if(resultCode==RESULT_OK&&data!=null&&data.getData()!=null){
      try{getContentResolver().takePersistableUriPermission(data.getData(),android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);}catch(Exception e){}
      if(requestCode==400){selectedMedia=data.getData().toString();Toast.makeText(this,"Фото добавлено к публикации",Toast.LENGTH_SHORT).show();}
      if(requestCode==402){Toast.makeText(this,"История выбрана. Публикация историй потребует сервер.",Toast.LENGTH_SHORT).show();}
    }}
  void activity(){heading("Активность");tabs(new String[]{"Все","Лайки","Ответы","Подписки"},0,i->{});row("♥  adi.kaz","Понравилась ваша публикация · 5 мин",()->{});row("▢  bakyt.uz","Ответил на вашу публикацию · 12 мин",()->{});row("♙  erol.kg","Подписался на вас · 23 мин",()->{});row("♥  temir.kgz","И ещё 24 человека лайкнули пост · 1 ч",()->{});}
  void profile(){View cover=new View(this);cover.setBackground(new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{Color.rgb(74,29,53),BG}));content.addView(cover,lp(-1,125));ImageView avatar=photo("avatar_profile.jpg",55);LinearLayout.LayoutParams a=lp(80,80);a.setMargins(dp(18),dp(-25),0,0);content.addView(avatar,a);
    TextView name=text(prefs.getString("display_name","aidar.kg"),23,WHITE,true);pad(name,18,10,18,2);content.addView(name);TextView handle=text(prefs.getString("handle","@aidar.kg")+" · "+prefs.getString("city","Бишкек")+" 🇰🇬",13,MUTED,false);pad(handle,18,0,18,15);content.addView(handle);
    TextView bio=text(prefs.getString("bio","Знакомства · Общение · Новые люди 💜\nСпорт · Путешествия · Кино"),14,WHITE,false);pad(bio,18,0,18,17);content.addView(bio);
    LinearLayout buttons=horizontal();pad(buttons,18,0,18,18);buttons.addView(button("Редактировать",()->editProfile()));TextView settings=button("⚙ Настройки",()->settings());LinearLayout.LayoutParams sp=lp(-2,-2);sp.setMargins(dp(9),0,0,0);buttons.addView(settings,sp);content.addView(buttons);TextView stats=text("248 Подписки        1 392 Подписчика        312 Подписки",13,WHITE,true);pad(stats,18,12,18,15);content.addView(stats);tabs(new String[]{"▦","▤","♧"},0,i->{});TextView empty=text("Публикаций пока нет",14,MUTED,false);empty.setGravity(Gravity.CENTER);pad(empty,0,45,0,0);content.addView(empty);
  }
  void settings(){options("Настройки",new String[]{"Конфиденциальность","Оформление","Уведомления","Сохранённое","О Menly"},i->{if(i==0)privacy();if(i==1)themes();if(i==2)notifications();if(i==3)saved();if(i==4)dialog("Menly","Нативное Android-приложение на Java. Firebase ещё не подключён.");});}
  void notifications(){new AlertDialog.Builder(this).setTitle("Уведомления").setItems(new String[]{"Все","Упоминания","Подписки","Сообщения"},(d,i)->dialog("Уведомления","События будут появляться здесь после подключения Firebase")).setPositiveButton("Готово",null).show();}
  void saved(){dialog("Сохранённые публикации","Сохранённые публикации будут отображаться здесь после подключения аккаунтов.");}
  void editProfile(){
    LinearLayout form=vertical();pad(form,20,5,20,5);
    EditText name=new EditText(this),handle=new EditText(this),city=new EditText(this),bio=new EditText(this);
    name.setSingleLine();handle.setSingleLine();city.setSingleLine();
    name.setHint("Имя");name.setText(prefs.getString("display_name","Мой профиль"));
    handle.setHint("Никнейм");handle.setText(prefs.getString("handle","menly_user"));
    city.setHint("Город");city.setText(prefs.getString("city","Бишкек"));
    bio.setHint("О себе");bio.setText(prefs.getString("bio",""));
    for(EditText e:new EditText[]{name,handle,city,bio})form.addView(e,lp(-1,-2));
    new AlertDialog.Builder(this).setTitle("Редактировать профиль").setView(form).setNegativeButton("Отмена",null).setPositiveButton("Сохранить",(d,w)->{prefs.edit().putString("display_name",name.getText().toString()).putString("handle",handle.getText().toString()).putString("city",city.getText().toString()).putString("bio",bio.getText().toString()).apply();show(4);}).show();
  }
  void privacy(){
    String[] choices={"Все пользователи","Только подписчики","Никто"};
    int current=prefs.getInt("dm_privacy",0);
    new AlertDialog.Builder(this).setTitle("Кто может мне писать").setSingleChoiceItems(choices,current,(d,i)->{prefs.edit().putInt("dm_privacy",i).apply();d.dismiss();}).setNeutralButton(prefs.getBoolean("online",true)?"Скрыть статус онлайн":"Показывать онлайн",(d,w)->{prefs.edit().putBoolean("online",!prefs.getBoolean("online",true)).apply();}).setNegativeButton("Закрыть",null).show();
  }
  void themes(){String[] names={"Бордовый","Розовый","Малиновый","Фиолетовый","Синий","Бирюзовый","Оранжевый","Красный","Коричневый","Серый","Чёрный","Белый"};int[] colors={0xff972b52,0xffd34872,0xffae477f,0xff6b4fc4,0xff3e7dc4,0xff1d9889,0xffde8e38,0xffa13c3c,0xff7c5e45,0xff575757,0xff333333,0xffeeeeee};options("Тема оформления",names,i->{accent=colors[i];prefs.edit().putInt("accent",accent).apply();show(page);});}
  void options(String title,String[] items,Select sel){new AlertDialog.Builder(this).setTitle(title).setItems(items,(d,w)->sel.choose(w)).setNegativeButton("Отмена",null).show();}
  void dialog(String title,String msg){new AlertDialog.Builder(this).setTitle(title).setMessage(msg).setPositiveButton("OK",null).show();}
  @Override public void onBackPressed(){if(page!=0)show(0);else super.onBackPressed();}
}