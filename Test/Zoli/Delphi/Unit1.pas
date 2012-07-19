unit Unit1;

interface

uses
  Windows, Messages, SysUtils, Classes, Graphics, Controls, Forms, Dialogs,
  SerialComUnit,GLScene, GLObjects, GLMisc, GLWin32Viewer, ComCtrls, ThdTimer,
  GLCadencer,GLTexture, GLGeomObjects, StdCtrls, sCheckedControl,
  sCheckbox, sEdit, sPanel, sMemo, ExtCtrls, Buttons, yupack, IAgaloLED,
  sButtonControl, sCustomButton;

type
  TForm1 = class(TForm)
    GLSceneViewer1: TGLSceneViewer;
    GLScene1: TGLScene;
    GLCamera1: TGLCamera;
    GLCube1: TGLCube;
    focal: TTrackBar;
    xaxisrot: TTrackBar;
    yaxisrot: TTrackBar;
    zaxisrot: TTrackBar;
    ThreadedTimer1: TThreadedTimer;
    GLCadencer1: TGLCadencer;
    GLLightSource1: TGLLightSource;
    GLSphere1: TGLSphere;
    GLMaterialLibrary1: TGLMaterialLibrary;
    ThreadedTimer2: TThreadedTimer;
    sPanel1: TsPanel;
    sContainer1: TsContainer;
    Label3: TLabel;
    sCheckBox1: TsCheckBox;
    sEdit2: TsEdit;
    Label4: TLabel;
    Label5: TLabel;
    SpeedButton1: TSpeedButton;
    SpeedButton2: TSpeedButton;
    SpeedButton3: TSpeedButton;
    SpeedButton4: TSpeedButton;
    Memo1: TMemo;
    SpeedButton5: TSpeedButton;
    IAgaloLED1: TIAgaloLED;
    Shape1: TShape;
    IAgaloLED2: TIAgaloLED;
    YusoftGradientButton2: TYusoftGradientButton;
    YusoftGradientButton1: TYusoftGradientButton;
    sMemo1: TsMemo;
    Label1: TLabel;
    sEdit1: TsEdit;
    Label2: TLabel;
    Label6: TLabel;
    sButton1: TsButton;
    sButton2: TsButton;
    sButton3: TsButton;
    sMemo2: TsMemo;
    sButton4: TsButton;
    YusoftGradientButton3: TYusoftGradientButton;
    Label7: TLabel;
    sButton5: TsButton;
    sButton6: TsButton;
    sButton7: TsButton;
    sButton8: TsButton;
    sButton9: TsButton;
    sButton10: TsButton;
    sButton11: TsButton;
    sMemo3: TsMemo;
    procedure focalChange(Sender: TObject);
    procedure xaxisrotChange(Sender: TObject);
    procedure yaxisrotChange(Sender: TObject);
    procedure zaxisrotChange(Sender: TObject);
    procedure ThreadedTimer1Timer(Sender: TObject);
    procedure GLCadencer1Progress(Sender: TObject; const deltaTime,
      newTime: Double);
    procedure FormClose(Sender: TObject; var Action: TCloseAction);
    procedure FormCreate(Sender: TObject);
    procedure GLSceneViewer1MouseMove(Sender: TObject; Shift: TShiftState;
      X, Y: Integer);
    procedure ThreadedTimer2Timer(Sender: TObject);
    procedure SpeedButton1Click(Sender: TObject);
    procedure SpeedButton2Click(Sender: TObject);
    procedure SpeedButton3Click(Sender: TObject);
    procedure SpeedButton5Click(Sender: TObject);
    procedure sButton1Click(Sender: TObject);
    procedure sButton3Click(Sender: TObject);
    procedure sButton2Click(Sender: TObject);
    procedure sButton4Click(Sender: TObject);
    procedure sButton5Click(Sender: TObject);
    procedure sButton6Click(Sender: TObject);
    procedure sButton7Click(Sender: TObject);
    procedure sButton8Click(Sender: TObject);
    procedure sButton9Click(Sender: TObject);
    procedure sButton10Click(Sender: TObject);
    procedure sButton11Click(Sender: TObject);
  private
    { Private declarations }

 SerialCom1: TSerialFile;


  public
    { Public declarations }
  end;

var
  Form1: TForm1;
  csored:byte;
  novi,cnt,cntb,strcntfull,strcnt:integer;
  oldpick : TGLCustomSceneObject; //global
  portnum:Integer;
  disp,bfa,read_buff: array[0..20000] of byte;
  tl:Integer;
  tbl : array[0..1255] of byte;
  tbl_ptr,ledoffcnt:Integer;
  str,str2:String;
  eredmeny,erstr:String;
  mertsuly:Single;

implementation

{$R *.DFM}

procedure TForm1.focalChange(Sender: TObject);
begin
 GLCamera1.focallength:=focal.position;

end;

procedure TForm1.xaxisrotChange(Sender: TObject);
begin
Glcube1.turnangle:=xaxisrot.position;
end;

procedure TForm1.yaxisrotChange(Sender: TObject);
begin
Glcube1.pitchangle:=yaxisrot.position;
end;

procedure TForm1.zaxisrotChange(Sender: TObject);
begin
Glcube1.rollangle:=zaxisrot.position;
end;

procedure TForm1.ThreadedTimer1Timer(Sender: TObject);
begin
//form1.Caption:=Format('%.2f FPS', [GLSceneViewer1.FramesPerSecond]);
//GLSceneViewer1.ResetPerformanceMonitor;
if ledoffcnt>0 then dec(ledoffcnt);
if ledoffcnt<1 then
  IAgaloLED1.LedOn:=False;


end;

procedure TForm1.GLCadencer1Progress(Sender: TObject; const deltaTime,
  newTime: Double);
begin
 Glcube1.turnangle:=newtime*23;
 Glcube1.pitchangle:=newtime*23;
 Glcube1.rollangle:=newtime*45;
 csored:=csored+novi;
 if csored>250 then novi:=-1;
 if csored<5 then novi:=1;

 GLMaterialLibrary1.Materials.Items[0].Material.FrontProperties.Diffuse. AsWinColor:=rgb(csored,csored,58);
// Glcube1.Scale.X:=1+(csored / 168);
end;

procedure TForm1.FormClose(Sender: TObject; var Action: TCloseAction);
begin
 ThreadedTimer1.Enabled:=False;
 SerialCom1.Close;
end;

procedure TForm1.FormCreate(Sender: TObject);
var
 i:Integer;
begin
novi:=1;
cntb:=0;
cnt:=1;
strcntfull:=0;
strcnt:=0;
eredmeny:='                                                                                      ';

 SerialCom1:=TSerialCom.Create(Self);
 SerialCom1.BaudRate:= 115200;
// SerialCom1.BaudRate:= 9600;
 SerialCom1.DataBits:=8;
 SerialCom1.Parity:=prNone;
 SerialCom1.StopBits:=sbOneStopBit;
// SerialCom1.StopBits:= sbTwoStopBits;
 SerialCom1.Port:=1;
 SerialCom1.Open;
 {
 for i:=0 to 10000 do begin
   SerialCom1.WriteString('a'); sleep(1);
 end;
  }
 ThreadedTimer2.Enabled:=True;
end;

procedure TForm1.GLSceneViewer1MouseMove(Sender: TObject;
  Shift: TShiftState; X, Y: Integer);
var
	pick : TGLCustomSceneObject;
begin
	// if an object is picked...
	pick:=(GLSceneViewer1.Buffer.GetPickedObject(x, y) as TGLCustomSceneObject);
	if Assigned(pick) then begin
		// ...turn it to yellow and show its name
		pick.Material.FrontProperties.Emission.Color:=clrYellow;
		ShowMessage('You clicked the '+pick.Name);
	end;
end;
//-------------------------------------------------------------
procedure TForm1.ThreadedTimer2Timer(Sender: TObject);
var
  s,i,j,d,h: Integer;
begin
 s:=SerialCom1.Read(bfa[0],5000);  // -- Soros buffer feltöltés
 if s>0 then begin
  IAgaloLED1.LedOn:=True; ledoffcnt:=4;
  for i:=1 to s do begin
    eredmeny[cnt]:=char(bfa[i-1]);
    sMemo3.Lines.Add(eredmeny);
    erstr:=erstr+(' '+char(bfa[i-1])+' ');

    read_buff[cntb]:=bfa[i-1];
    inc(cnt);
    if (bfa[i-1]=$0a) then begin
      setlength(eredmeny,cnt-3);
      sMemo1.Lines.Add(eredmeny);
      cnt:=1;  cntb:=1;
      eredmeny:='                                                                                   ';
    end;
    {
    str2:=str2+char(read_buff[cntb]);//  +inttohex(2,read_buff[cntb]);
    str:=str+inttohex(read_buff[cntb],2);
    inc(strcnt);
    inc(strcntfull);
    if strcnt=7 then str:=str+'  '+#9
    else             str:=str+' ';
    if strcnt=15 then begin
      str:=str+' '+#9+str2;
      if sCheckBox1.Checked=True then
        sMemo1.Lines.Add(str);
      str2:='';
      str:='';
      strcnt:=0;
    end else   str:=str+' ';
  end;
  Memo1.Lines.Add(erstr);
  erstr:='';
  }
  end;
  inc(cntb);
 // str:=str+' '+#9+str//  if sCheckBox1.Checked=True then
//    sMemo1.Lines.Add(str);
//  str2:='';
//  strcnt:=0;

//  if sCheckBox1.Checkd=True then
//     Memo1.Lines.Add(eredmeny);
 end;
end;

procedure TForm1.SpeedButton1Click(Sender: TObject);
begin
 SerialCom1.WriteString(char(2));
  sleep(25);
 SerialCom1.WriteString(char($41));
 sleep(25);
  SerialCom1.WriteString(char($40));
  sleep(25);
 SerialCom1.WriteString(char($38));
  sleep(25);
 SerialCom1.WriteString(char($31));
  sleep(25);
 SerialCom1.WriteString(char($1a));
end;

procedure TForm1.SpeedButton2Click(Sender: TObject);
begin
 SerialCom1.WriteString('c');
 sleep(25);
 SerialCom1.WriteString('*');

end;

procedure TForm1.SpeedButton3Click(Sender: TObject);
begin
 SerialCom1.WriteString('t');
 sleep(25);
 SerialCom1.WriteString('*');

end;

procedure TForm1.SpeedButton5Click(Sender: TObject);
begin
  SerialCom1.WriteString(sEdit2.Text+#$0d+#$0a);
  sleep(25);
end;

procedure TForm1.sButton1Click(Sender: TObject);
begin
  SerialCom1.WriteString('at'+#$0d+#$0a);
end;

procedure TForm1.sButton3Click(Sender: TObject);
begin
  SerialCom1.WriteString('set'+#$0d+#$0a);
end;

procedure TForm1.sButton2Click(Sender: TObject);
begin
 // SerialCom1.WriteString(sEdit1.Text+#$0d+#$0a);
  SerialCom1.WriteString(sEdit1.Text+#$0a);
  Form1.IAgaloLED2.ledon:=True;
end;

procedure TForm1.sButton4Click(Sender: TObject);
var
 i,j:Integer;
 str :string;
begin
  for i:=1 to sMemo2.Lines.Capacity do begin
//    showmessage(sMemo2.Lines.Strings[i]);
    str:=sMemo2.Lines.Strings[i-1];
    SerialCom1.WriteString(str+#$0d+#$0a);
    sleep(10);
  end;
  //showmessage(inttostr(sMemo2.Lines.Capacity));
  //SerialCom1.WriteString(sEdit1.Text+#$0d+#$0a);
end;

procedure TForm1.sButton5Click(Sender: TObject);
begin
sMemo1.Clear;
end;

procedure TForm1.sButton6Click(Sender: TObject);
begin
  SerialCom1.WriteString('RESET'+#$0d+#$0a);
end;

procedure TForm1.sButton7Click(Sender: TObject);
begin
    SerialCom1.WriteString('INFO'+#$0d+#$0a);
end;

procedure TForm1.sButton8Click(Sender: TObject);
begin
   SerialCom1.WriteString('LIST'+#$0d+#$0a);
end;

procedure TForm1.sButton9Click(Sender: TObject);
begin
   SerialCom1.WriteString('SET BT NAME PALINCAR'+#$0d+#$0a);
end;

procedure TForm1.sButton10Click(Sender: TObject);
begin
      SerialCom1.WriteString('IC'+#$0d+#$0a);
end;

procedure TForm1.sButton11Click(Sender: TObject);
begin
SerialCom1.WriteString('INQUIRY 5'+#$0d+#$0a);

end;

end.
