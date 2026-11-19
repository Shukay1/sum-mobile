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
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import java.io.InputStream;

public class MainActivity extends Activity {
  final int BG=Color.rgb(8,8,11), CARD=Color.rgb(29,27,34), LINE=Color.rgb(43,39,49), WHITE=Color.WHITE, MUTED=Color.rgb(157,150,165);
  int accent=Color.rgb(169,52,230), page=0, activityFilter=0, profileTab=0; boolean anon=false; int radiusKm=10; String draft=""; String selectedMedia=""; String audience="Вся Центральная Азия", chatMode="Личные";
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
  class NavIcon extends View{
    final int type;final Paint paint=new Paint(3);NavIcon(int n){super(MainActivity.this);type=n;setContentDescription(new String[]{"Лента","Чаты","Создать","Активность","Профиль"}[n]);}
    @Override protected void onDraw(Canvas c){super.onDraw(c);float cx=getWidth()/2f,cy=getHeight()/2f,unit=dp(1);c.save();c.translate(cx,cy);c.scale(unit,unit);
      paint.setColor(type==page?accent:MUTED);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(2.1f);paint.setStrokeCap(Paint.Cap.ROUND);paint.setStrokeJoin(Paint.Join.ROUND);
      Path p=new Path();
      if(type==0){p.moveTo(-11,-1);p.lineTo(0,-11);p.lineTo(11,-1);p.lineTo(11,11);p.lineTo(3,11);p.lineTo(3,3);p.lineTo(-3,3);p.lineTo(-3,11);p.lineTo(-11,11);p.close();c.drawPath(p,paint);}
      if(type==1){c.drawRoundRect(-11,-10,11,8,7,7,paint);p.moveTo(-4,8);p.lineTo(-9,13);p.lineTo(-8,5);c.drawPath(p,paint);}
      if(type==2){c.drawRoundRect(-11,-11,11,11,5,5,paint);c.drawLine(-6,0,6,0,paint);c.drawLine(0,-6,0,6,paint);}
      if(type==3){p.moveTo(0,11);p.cubicTo(-4,7,-12,1,-11,-5);p.cubicTo(-10,-13,-2,-13,0,-7);p.cubicTo(4,-13,12,-13,11,-5);p.cubicTo(12,1,4,7,0,11);c.drawPath(p,paint);}
      if(type==4){c.drawCircle(0,-6,5,paint);c.drawArc(-11,1,11,15,190,160,false,paint);}
      c.restore();
    }
  }
  void bottom(){
    line(root);nav=horizontal();nav.setBackgroundColor(BG);
    for(int i=0;i<5;i++){final int n=i;NavIcon b=new NavIcon(i);nav.addView(b,new LinearLayout.LayoutParams(0,dp(62),1));b.setOnClickListener(v->show(n));}
    root.addView(nav);
  }
  void feed(){
    LinearLayout bar=horizontal();pad(bar,20,12,20,8);TextView logo=text("♡",34,accent,true);bar.addView(logo,new LinearLayout.LayoutParams(0,dp(46),1));TextView bell=button("♧",()->notifications());bar.addView(bell);content.addView(bar);
    tabs(new String[]{"Для тебя","Подписки"},0,i->{if(i==1)dialog("Подписки","Подписки будут синхронизироваться после подключения сервера");});
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

    if(chatMode.equals("Запросы")){row("Айдар · Новый запрос","Нажми, чтобы принять или отклонить",()->requests());row("Бек · Новый запрос","Нажми, чтобы принять или отклонить",()->requests());return;}
    if(chatMode.equals("Группы"))row("＋ Создать группу","Публичная или приватная",()->newGroup());
    String[] names=chatMode.equals("Группы")?groups.toArray(new String[0]):new String[]{"Азамат","Бек","Нурлан","Эмир","Руслан"};
    String[] previews={"Привет! Как дела? 😄","Отправил фото","Давай вечером пообщаемся?","Стикер","Спасибо! 💜"};
    String[] assets={"avatar_azamat.jpg","avatar_nurlan.jpg","avatar_emir.jpg","avatar_profile.jpg","avatar_azamat.jpg"};
    for(int j=0;j<names.length;j++){
      final String n=names[j];LinearLayout lineRow=horizontal();pad(lineRow,18,12,18,12);
      ImageView av=photo(assets[j%assets.length],100);lineRow.addView(av,lp(52,52));
      LinearLayout info=vertical();pad(info,13,0,0,0);info.addView(text(n,15,WHITE,true));TextView preview=text(chatMode.equals("Группы")?"Групповой чат":previews[j%previews.length],12,MUTED,false);pad(preview,0,5,0,0);info.addView(preview);
      lineRow.addView(info,new LinearLayout.LayoutParams(0,-2,1));
      lineRow.addView(text(j==0?"14:32":"Вчера",11,MUTED,false));
      content.addView(lineRow);line(content);lineRow.setOnClickListener(v->conversation(n));
    }
  }
  void chatTabs(){
    LinearLayout bar=horizontal();pad(bar,16,4,16,8);
    String[] labels={"Личные","Группы","Запросы"};
    for(int i=0;i<3;i++){final int k=i;boolean active=chatMode.equals(labels[i]);TextView tab=text(labels[i],14,active?WHITE:MUTED,active);tab.setGravity(Gravity.CENTER);tab.setBackground(shape(active?accent:CARD,18));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(40),1);p.setMargins(0,0,dp(7),0);bar.addView(tab,p);tab.setOnClickListener(v->{chatMode=labels[k];show(1);});}
    content.addView(bar);line(content);
  }
  void requests(){new AlertDialog.Builder(this).setTitle("Запросы на переписку").setItems(new String[]{"Новый запрос · @aidar.kg","Новый запрос · @bek.kg","Новый запрос · @erlan.kg"},(d,w)->new AlertDialog.Builder(this).setTitle("Запрос").setMessage("Принять запрос на переписку?").setNegativeButton("Отклонить",null).setPositiveButton("Принять",(a,b)->conversation(new String[]{"Айдар","Бек","Эрлан"}[w])).show()).setNegativeButton("Закрыть",null).show();}
  void newGroup(){
    root=vertical();root.setBackgroundColor(BG);setContentView(root);
    LinearLayout head=horizontal();pad(head,15,12,15,12);head.addView(button("×",()->show(1)));
    TextView title=text("Создать группу",18,WHITE,true);title.setGravity(Gravity.CENTER);head.addView(title,new LinearLayout.LayoutParams(0,dp(45),1));root.addView(head);line(root);
    LinearLayout form=vertical();pad(form,18,30,18,0);root.addView(form);
    TextView avatar=text("♙+",30,accent,true);avatar.setGravity(Gravity.CENTER);avatar.setBackground(shape(CARD,100));LinearLayout.LayoutParams av=lp(82,82);av.gravity=Gravity.CENTER;av.bottomMargin=dp(23);form.addView(avatar,av);
    EditText name=new EditText(this);name.setHint("Название группы");name.setTextColor(WHITE);name.setHintTextColor(MUTED);name.setSingleLine();name.setBackground(shape(CARD,12));pad(name,12,10,12,10);form.addView(name,lp(-1,48));
    EditText description=new EditText(this);description.setHint("Описание (необязательно)");description.setTextColor(WHITE);description.setHintTextColor(MUTED);description.setBackground(shape(CARD,12));pad(description,12,10,12,10);LinearLayout.LayoutParams dp=lp(-1,48);dp.topMargin=dp(10);form.addView(description,dp);
    TextView label=text("Тип группы",16,WHITE,true);pad(label,0,25,0,12);form.addView(label);
    RadioGroup group=new RadioGroup(this);RadioButton privateBtn=new RadioButton(this),publicBtn=new RadioButton(this);
    privateBtn.setText("Закрытая · Вступление по приглашению");publicBtn.setText("Открытая · Могут присоединиться по ссылке");
    privateBtn.setTextColor(WHITE);publicBtn.setTextColor(WHITE);privateBtn.setButtonTintList(android.content.res.ColorStateList.valueOf(accent));publicBtn.setButtonTintList(android.content.res.ColorStateList.valueOf(accent));group.addView(privateBtn);group.addView(publicBtn);privateBtn.setChecked(true);form.addView(group);
    TextView create=button("Создать группу",()->{String value=name.getText().toString().trim();if(value.isEmpty()){name.setError("Укажи название");return;}groups.add(value);prefs.edit().putBoolean("group_private_"+value,privateBtn.isChecked()).putString("group_desc_"+value,description.getText().toString()).apply();persist();chatMode="Группы";show(1);});create.setBackground(shape(accent,13));LinearLayout.LayoutParams cp=lp(-1,51);cp.topMargin=dp(26);form.addView(create,cp);
  }
  void conversation(String name){
    root=vertical();root.setBackgroundColor(BG);setContentView(root);LinearLayout head=horizontal();TextView back=button("‹",()->show(1));head.addView(back);TextView title=text(name,19,WHITE,true);pad(title,15,0,0,0);head.addView(title);pad(head,10,12,0,12);root.addView(head);line(root);
    ScrollView sv=new ScrollView(this);LinearLayout msgs=vertical();pad(msgs,16,15,16,15);sv.addView(msgs);root.addView(sv,new LinearLayout.LayoutParams(-1,0,1));
    ArrayList<String> history=messages.computeIfAbsent(name,k->new ArrayList<>());
    if(history.isEmpty()){try{JSONArray saved=new JSONArray(prefs.getString("chat_"+name,"[]"));for(int j=0;j<saved.length();j++)history.add(saved.getString(j));}catch(Exception e){}}
    if(history.isEmpty())history.add("Привет! Как дела?");
    for(String m:history){TextView bubble=text(m,15,WHITE,false);pad(bubble,14,11,14,11);bubble.setBackground(shape(CARD,16));LinearLayout.LayoutParams b=lp(-2,-2);b.setMargins(0,0,0,dp(10));msgs.addView(bubble,b);}
    LinearLayout compose=horizontal();pad(compose,10,7,10,12);EditText entry=new EditText(this);entry.setSingleLine();entry.setTextColor(WHITE);entry.setHint("Сообщение");entry.setHintTextColor(MUTED);entry.setBackground(shape(CARD,22));pad(entry,13,9,13,9);compose.addView(entry,new LinearLayout.LayoutParams(0,dp(46),1));TextView send=button("➤",()->{String m=entry.getText().toString().trim();if(!m.isEmpty()){history.add(m);prefs.edit().putString("chat_"+name,new JSONArray(history).toString()).apply();conversation(name);}});compose.addView(send);root.addView(compose);
  }
  void create(){heading("Новая публикация");EditText field=new EditText(this);field.setId(881);field.setText(draft);field.setTextColor(WHITE);field.setHintTextColor(MUTED);field.setHint("О чём думаешь?");field.setGravity(Gravity.TOP);field.setMinLines(7);field.setBackground(shape(CARD,15));pad(field,16,15,16,15);LinearLayout.LayoutParams f=lp(-1,-2);f.setMargins(dp(18),0,dp(18),dp(10));content.addView(field,f);
    row("Анонимная публикация",anon?"Включено · исчезнет через 24 часа":"Выключено",()->{anon=!anon;draft=field.getText().toString();show(2);});
    row("Кто увидит публикацию",audience,()->{draft=field.getText().toString();audienceScreen();});
    row("Добавить фото или видео",selectedMedia.isEmpty()?"Выбрать из галереи":"Фото выбрано ✓",()->chooseMedia());
    TextView publish=button("Опубликовать",()->{String s=field.getText().toString().trim();if(s.isEmpty()){Toast.makeText(this,"Напиши текст",Toast.LENGTH_SHORT).show();return;}Post created=new Post(anon?"Анонимный пользователь":"menly_user","сейчас",s,0,0,0,anon,false);created.media=selectedMedia;posts.add(0,created);selectedMedia="";draft="";anon=false;persist();show(0);});publish.setBackground(shape(accent,14));LinearLayout.LayoutParams p=lp(-1,52);p.setMargins(dp(18),dp(20),dp(18),0);content.addView(publish,p);
  }
  void subScreen(String title){
    root=vertical();root.setBackgroundColor(BG);setContentView(root);
    LinearLayout head=horizontal();pad(head,12,9,15,9);
    TextView back=button("×",()->show(2));head.addView(back,lp(45,43));
    TextView h=text(title,17,WHITE,true);h.setGravity(Gravity.CENTER);head.addView(h,new LinearLayout.LayoutParams(0,dp(43),1));
    head.addView(new View(this),lp(45,43));root.addView(head);line(root);
    scroll=new ScrollView(this);content=vertical();scroll.addView(content);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
  }
  void audienceScreen(){
    subScreen("Аудитория публикации");
    row("◎  Рядом со мной","Выбрать радиус (1–100 км)"+(audience.endsWith("км")?" · "+audience:""),()->radiusScreen());
    row("🇰🇬  Вся моя страна","Кыргызстан",()->{audience="Моя страна";show(2);});
    row("◉  Вся Центральная Азия","Кыргызстан, Казахстан, Узбекистан",()->{audience="Вся Центральная Азия";show(2);});
    TextView tip=text("Выбранная аудитория: "+audience,13,accent,false);pad(tip,20,25,20,0);content.addView(tip);
  }
  void radiusScreen(){
    subScreen("Радиус");
    TextView heading=text("Радиус",20,WHITE,true);pad(heading,20,35,20,10);content.addView(heading);
    TextView current=text(radiusKm+" км",22,WHITE,true);current.setGravity(Gravity.RIGHT);pad(current,20,0,25,15);content.addView(current);
    SeekBar seek=new SeekBar(this);seek.setMax(100);seek.setProgress(radiusKm);seek.setProgressTintList(android.content.res.ColorStateList.valueOf(accent));seek.setThumbTintList(android.content.res.ColorStateList.valueOf(accent));pad(seek,20,12,20,12);content.addView(seek);seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
      public void onProgressChanged(SeekBar b,int p,boolean user){radiusKm=Math.max(1,p);current.setText(radiusKm+" км");}
      public void onStartTrackingTouch(SeekBar b){}public void onStopTrackingTouch(SeekBar b){}
    });
    LinearLayout choices=horizontal();pad(choices,15,14,15,0);int[] values={1,5,10,25,50,100};
    for(int value:values){TextView chip=button(value+" км",()->{radiusKm=value;audience=value+" км";show(2);});chip.setTextSize(11);LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(0,dp(38),1);cp.setMargins(dp(2),0,dp(2),0);choices.addView(chip,cp);}
    content.addView(choices);
    TextView confirm=button("Применить радиус",()->{audience=radiusKm+" км";show(2);});confirm.setBackground(shape(accent,13));LinearLayout.LayoutParams c=lp(-1,50);c.setMargins(dp(20),dp(30),dp(20),0);content.addView(confirm,c);
  }
  void chooseMedia(){android.content.Intent intent=new android.content.Intent(android.content.Intent.ACTION_OPEN_DOCUMENT);intent.addCategory(android.content.Intent.CATEGORY_OPENABLE);intent.setType("image/*");intent.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION|android.content.Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);startActivityForResult(intent,400);}
  @Override protected void onActivityResult(int requestCode,int resultCode,android.content.Intent data){super.onActivityResult(requestCode,resultCode,data);if(resultCode==RESULT_OK&&data!=null&&data.getData()!=null){
      try{getContentResolver().takePersistableUriPermission(data.getData(),android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);}catch(Exception e){}
      if(requestCode==400){selectedMedia=data.getData().toString();Toast.makeText(this,"Фото добавлено к публикации",Toast.LENGTH_SHORT).show();}
      if(requestCode==402){Toast.makeText(this,"История выбрана. Публикация историй потребует сервер.",Toast.LENGTH_SHORT).show();}
    }}
  void activity(){
    heading("Лайки");
    String[] filters={"Все","Лайки","Ответы","Подписки"};
    LinearLayout bar=horizontal();pad(bar,13,2,13,15);
    for(int i=0;i<filters.length;i++){final int k=i;TextView chip=button(filters[i],()->{activityFilter=k;show(3);});chip.setTextSize(12);chip.setBackground(shape(i==activityFilter?accent:CARD,24));LinearLayout.LayoutParams c=new LinearLayout.LayoutParams(0,dp(38),1);c.setMargins(dp(3),0,dp(3),0);bar.addView(chip,c);}content.addView(bar);
    String[] names={"adi.kaz","bakyt.uz","erol.kg","temir.kgz","sanzhar.kaz"};
    String[] descriptions={"поставил(а) лайк вашему посту · 5 мин","ответил(а) на ваш пост: «Полностью согласен!» · 12 мин","подписался(ась) на вас · 23 мин","и ещё 24 человека лайкнули вашу публикацию · 1 ч","сделал(а) репост · 2 ч"};
    int[] types={1,2,3,1,0};
    String[] avatars={"avatar_azamat.jpg","avatar_nurlan.jpg","avatar_emir.jpg","avatar_profile.jpg","avatar_azamat.jpg"};
    for(int i=0;i<names.length;i++){
      if(activityFilter!=0&&activityFilter!=types[i])continue;
      final String name=names[i];LinearLayout r=horizontal();pad(r,16,13,16,13);r.addView(photo(avatars[i],100),lp(45,45));
      LinearLayout details=vertical();pad(details,11,0,0,0);details.addView(text(name,14,WHITE,true));
      TextView desc=text(descriptions[i],12,MUTED,false);pad(desc,0,5,0,0);details.addView(desc);
      r.addView(details,new LinearLayout.LayoutParams(0,-2,1));
      TextView symbol=text(types[i]==1?"♥":types[i]==2?"▢":types[i]==3?"♙":"⇄",19,types[i]==1?0xfffa476f:accent,true);r.addView(symbol);
      content.addView(r);line(content);r.setOnClickListener(v->dialog("Активность",name+" · "+descriptions[0]));
    }
  }
  void profile(){
    LinearLayout top=horizontal();pad(top,18,17,18,15);
    ImageView avatar=photo("avatar_profile.jpg",100);top.addView(avatar,lp(83,83));
    LinearLayout info=vertical();pad(info,13,5,0,0);
    info.addView(text(prefs.getString("display_name","aidar.kg"),22,WHITE,true));
    TextView handle=text(prefs.getString("handle","Он/его"),13,MUTED,false);pad(handle,0,5,0,0);info.addView(handle);
    top.addView(info,new LinearLayout.LayoutParams(0,-2,1));
    TextView setting=button("⚙",()->settings());top.addView(setting);content.addView(top);
    TextView bio=text(prefs.getString("bio","Бишкек 🇰🇬\nЗнакомства · Общение · Новые люди 💜\nСпорт · Путешествия · Кино"),14,WHITE,false);pad(bio,18,5,18,14);content.addView(bio);
    TextView edit=button("Редактировать профиль",()->editProfile());LinearLayout.LayoutParams ep=lp(-1,44);ep.setMargins(dp(18),0,dp(18),dp(19));content.addView(edit,ep);
    LinearLayout stats=horizontal();String[] counts={"248","1 392","312"},labels={"Публикаций","Подписчики","Подписки"};
    for(int i=0;i<3;i++){LinearLayout cell=vertical();cell.setGravity(Gravity.CENTER);TextView number=text(counts[i],17,WHITE,true);number.setGravity(Gravity.CENTER);cell.addView(number);TextView label=text(labels[i],11,MUTED,false);label.setGravity(Gravity.CENTER);cell.addView(label);stats.addView(cell,new LinearLayout.LayoutParams(0,dp(55),1));}
    content.addView(stats);
    tabs(new String[]{"▦","▤","♧"},profileTab,i->{profileTab=i;show(4);});
    if(profileTab==0){
      String[] imgs={"avatar_profile.jpg","city.jpg","wheel.jpg","city.jpg","wheel.jpg","city.jpg"};
      for(int r=0;r<2;r++){LinearLayout grid=horizontal();for(int c=0;c<3;c++){ImageView im=photo(imgs[r*3+c],0);LinearLayout.LayoutParams ip=new LinearLayout.LayoutParams(0,dp(127),1);ip.setMargins(dp(1),dp(1),dp(1),dp(1));grid.addView(im,ip);im.setOnClickListener(v->dialog("Публикация","Просмотр фотографии"));}content.addView(grid);}
    }else if(profileTab==1){for(Post p:posts)if(!p.anonymous&&p.author.equals("menly_user"))row(p.text,"Моя публикация",()->comments(p));}
    else row("Сохранённые","Открыть сохранённые публикации",()->saved());
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
    root=vertical();root.setBackgroundColor(BG);setContentView(root);
    LinearLayout header=horizontal();pad(header,12,10,12,10);header.addView(button("‹",()->show(4)));
    TextView title=text("Приватность",19,WHITE,true);title.setGravity(Gravity.CENTER);header.addView(title,new LinearLayout.LayoutParams(0,dp(46),1));root.addView(header);line(root);
    scroll=new ScrollView(this);content=vertical();scroll.addView(content);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
    row("♧  Номер телефона","Всегда скрыт",()->dialog("Номер телефона","Номер телефона не отображается в профиле"));
    row("▢  Кто может мне писать",new String[]{"Все пользователи","Только подписчики","Никто"}[prefs.getInt("dm_privacy",0)],()->options("Кто может мне писать",new String[]{"Все пользователи","Только подписчики","Никто"},i->{prefs.edit().putInt("dm_privacy",i).apply();privacy();}));
    privacyToggle("Показывать мой профиль в рекомендациях","Другие пользователи могут найти меня","discoverable",true);
    privacyToggle("Использовать моё местоположение","Только для выбора аудитории постов","location",true);
    privacyToggle("Показывать статус онлайн","Видимость статуса для других","online",true);
  }
  void privacyToggle(String label,String description,String key,boolean initial){
    LinearLayout row=horizontal();pad(row,18,16,18,16);LinearLayout texts=vertical();texts.addView(text(label,14,WHITE,false));TextView sub=text(description,11,MUTED,false);pad(sub,0,5,0,0);texts.addView(sub);row.addView(texts,new LinearLayout.LayoutParams(0,-2,1));
    Switch sw=new Switch(this);sw.setChecked(prefs.getBoolean(key,initial));sw.setThumbTintList(android.content.res.ColorStateList.valueOf(accent));sw.setOnCheckedChangeListener((v,checked)->prefs.edit().putBoolean(key,checked).apply());row.addView(sw);content.addView(row);line(content);
  }
  void themes(){String[] names={"Бордовый","Розовый","Малиновый","Фиолетовый","Синий","Бирюзовый","Оранжевый","Красный","Коричневый","Серый","Чёрный","Белый"};int[] colors={0xff972b52,0xffd34872,0xffae477f,0xff6b4fc4,0xff3e7dc4,0xff1d9889,0xffde8e38,0xffa13c3c,0xff7c5e45,0xff575757,0xff333333,0xffeeeeee};options("Тема оформления",names,i->{accent=colors[i];prefs.edit().putInt("accent",accent).apply();show(page);});}
  void options(String title,String[] items,Select sel){new AlertDialog.Builder(this).setTitle(title).setItems(items,(d,w)->sel.choose(w)).setNegativeButton("Отмена",null).show();}
  void dialog(String title,String msg){new AlertDialog.Builder(this).setTitle(title).setMessage(msg).setPositiveButton("OK",null).show();}
  @Override public void onBackPressed(){if(page!=0)show(0);else super.onBackPressed();}
}