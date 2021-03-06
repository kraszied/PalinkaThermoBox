// Serial communication component
//
// (c) PROPIX  Written by Pinter Gabor
// H-8000 Szekesfehervar, Krivanyi u. 15.
// Tel: +36 70 3999639
// Fax: +36 22 304326
// Email: propix@freemail, Pinter.Gabor@freemail.hu
// Web: http://www.propix.hu
//
// Revisions:
//  V5.01 2000-11-21  First release (shareware)
//  V6.0  2003-06-02  Free software, WinXP compatible
//  V6.1  2004-09-15  Smaller components
//  V6.2  2004-10-08  Timeouts
//  V6.3  2005-02-04  Purge at close



unit SerialComUnit;

interface

uses
	Windows, Classes, SysUtils, Dialogs;


type
	TStopBits = (sbOneStopBit, sbOneHalfStopBits, sbTwoStopBits);
	TParity = (prNone, prOdd, prEven, prMark, prSpace);
	TDTRControl = (dtrNone, dtrFlow);
	TRTSControl = (rtsNone, rtsFlow, rtsDirection);
	TDataBits = 5..8;
	TBaudRate = Longword;
	TComError = (ceFrame, ceParity, ceOverrun, ceBreak);
	TComErrors= set of TComError;
	TComEvent = (evRxChar, evRxFlag, evTxEmpty, evCTS, evDSR,  evDCD, evBreak, evError, evRing);
	TComEvents = set of TComEvent;


	// Basic functions: open, close, read, write
	TSerialFile = class(TComponent)
	private
		FHandle: THandle;								// Serial port file handle
		CloseEvent: THandle;								// Close signal to pending I/O
		FConnected: Boolean;								// Serial port open
		FPortNumber: Byte;								// Serial port number
		FBaudRate: TBaudRate;								// Baud rate
		FDataBits: TDataBits;								// Data bits
		FParity: TParity;								// Parity
		FStopBits: TStopBits;								// Stop bits
		FWriteTimeout: Cardinal;							// Write timeout
		FReadTimeout: Cardinal;								// Read timeout
		function ComString: String;							// Make serial port file name
		procedure CreateHandle;								// Open serial port file
		procedure DestroyHandle;							// Close serial port file
		procedure SetWriteTimeout(Value: Cardinal);					// Write timeout
		procedure SetReadTimeout(Value: Cardinal);					// Read timeout
		function ValidHandle: Boolean;							// Test if Handle is valid
		function GetComErrors: TComErrors;						// Get property ComErrors
	protected
		procedure Setup; virtual;							// Setup port parameters
		property Handle: THandle read FHandle;						// Serial port file handle
	published
		property Port: Byte read FPortNumber write FPortNumber;				// Serial port number
		property BaudRate: TBaudRate read FBaudRate write FBaudRate;			// Baud rate
		property DataBits: TDataBits read FDataBits write FDataBits;			// Data bits
		property Parity: TParity read FParity write FParity;				// Parity
		property StopBits: TStopBits read FStopBits write FStopBits;			// Stop bits
		property WriteTimeout: Cardinal read FWriteTimeout write SetWriteTimeout;	// Write timeout
		property ReadTimeout: Cardinal read FReadTimeout write SetReadTimeout;		// Read timeout
		property Connected: Boolean read FConnected;					// Serial port is open
		property ComErrors: TComErrors read GetComErrors;				// Report errors
		function Exist: Boolean;							// Check if port exists
		procedure Open;	virtual;							// Open serial port
		procedure Close; virtual;							// Close serial port
		function Write(var Buffer; Count: Integer): Integer;				// Write data to transmit FIFO
		function WriteString(Str: String): Integer;					// Write data to transmit FIFO 2
		function Put(B: Byte): Integer;							// Write byte to transmit FIFO
		function Read(var Buffer; Count: Integer): Integer;				// Read data from receive FIFO
		function ReadString(var Str: String; Count: Integer): Integer;			// Read data from receive FIFO 2
		function Get: Integer;								// Read byte from receive FIFO
		constructor Create(AOwner: TComponent); override;                           	// Create
		destructor Destroy; override;                                               	// Destroy
	end;



	// Basic + Control signals
	TSerialControls = class(TSerialFile)
	private
		FDTRControl: TDTRControl;							// Automatic DTR control function
		FRTSControl: TRTSControl;							// Automatic RTS control function
		FXONXOFFControl: Boolean;							// XON/XOFF flow control
		FDTRState: Boolean;								// DTR state if DTR is not automatic
		FRTSState: Boolean;								// RTS state if RTS is not automatic
		FTxBreak: Boolean;								// Transmit break
		FOnOpen: TNotifyEvent;								// After port opened event
		FOnClose: TNotifyEvent;								// Before port closed event
		procedure SetDTRState(Value: Boolean);						// Set property DTRState
		procedure SetRTSState(Value: Boolean);						// Set property RTSState
		function GetCTSState: Boolean;							// Get property CTSState
		function GetDSRState: Boolean;							// Get property DSRState
		function GetDCDState: Boolean;							// Get property DCDState
		function GetRIState: Boolean;							// Get property RIState
		procedure SetTxBreak(Value: Boolean);						// Set property TxBreak
	protected
		procedure Setup; override;							// Setup port parameters
	published
		property OnOpen: TNotifyEvent read FOnOpen write FOnOpen;			// After port opened event
		property OnClose: TNotifyEvent read FOnClose write FOnClose;			// Before port closed event
		property DTRControl: TDTRControl read FDTRControl write FDTRControl;		// Automatic DTR functions
		property RTSControl: TRTSControl read FRTSControl write FRTSControl;        	// Automatic RTS functions
		property XONXOFFControl: Boolean read FXONXOFFControl write FXONXOFFControl;	// XON/XOFF control
		property DTRState: Boolean read FDTRState write SetDTRState;	        	// Control DTR line if not automatic
		property RTSState: Boolean read FRTSState write SetRTSState;			// Control RTS line if not automatic
		property CTSState: Boolean read GetCTSState;					// Get CTS line state
		property DSRState: Boolean read GetDSRState;					// Get DSR line state
		property DCDState: Boolean read GetDCDState;					// Get DCD line state
		property RIState: Boolean read GetRIState;					// Get RI line state
		property TxBreak: Boolean read FTxBreak write SetTxBreak;			// Send transmit break
		procedure Open;	override;							// Open serial port
		constructor Create(AOwner: TComponent); override;                           	// Create
		destructor Destroy; override;                                               	// Destroy
	end;



	// Basic + Control signals + Waits
	TSerialWaits = class(TSerialControls)
	published
		procedure WaitFor(ComEvents: TComEvents; Timeout: Cardinal);
	end;



	// Basic + Control signals + Event handling
	TSerialEvents = class(TSerialControls)
	private
		EventThread: TThread; // TComThread;						// Interrupt handler thread
		FComEvents: TComEvents;								// Interrupt mask
		FOnRxChar: TNotifyEvent;							// Receive buffer is not empty event
		FOnTxEmpty: TNotifyEvent;							// Transmit buffer is empty event
		FOnRxBreak: TNotifyEvent;							// Start of break received event
		FOnRI: TNotifyEvent;								// RI rising edge detected event
		FOnCTS: TNotifyEvent;								// CTS changed state event
		FOnDSR: TNotifyEvent;								// DSR changed state event
		FOnDCD: TNotifyEvent;								// DCD changed state event
		FOnError: TNotifyEvent;								// Receive error event
		procedure SetComEvents(Value: TComEvents);					// Set property ComEvents
		procedure SetOnRxChar(Value: TNotifyEvent);					// Set property OnRxChar
		procedure SetOnTxEmpty(Value: TNotifyEvent);					// Set property OnTxEmpty
		procedure SetOnRxBreak(Value: TNotifyEvent);					// Set property OnRxBreak
		procedure SetOnRI(Value: TNotifyEvent);						// Set property OnRI
		procedure SetOnCTS(Value: TNotifyEvent);					// Set property OnCTS
		procedure SetOnDSR(Value: TNotifyEvent);					// Set property OnDSR
		procedure SetOnDCD(Value: TNotifyEvent);					// Set property OnDCD
		procedure SetOnError(Value: TNotifyEvent);					// Set property OnError
		property ComEvents: TComEvents read FComEvents write SetComEvents;		// Interrupt events
	protected
		procedure Setup; override;							// Setup port parameters
	published
		property OnRxChar: TNotifyEvent read FOnRxChar write SetOnRxChar;		// Receive buffer is not empty event
		property OnTxEmpty: TNotifyEvent read FOnTxEmpty write SetOnTxEmpty;		// Transmit buffer is empty event
		property OnRxBreak: TNotifyEvent read FOnRxBreak write SetOnRxBreak;		// Start of break received event
		property OnRI: TNotifyEvent read FOnRI write SetOnRI;				// RI rising edge detected event
		property OnCTS: TNotifyEvent read FOnCTS write SetOnCTS;			// CTS changed state event
		property OnDSR: TNotifyEvent read FOnDSR write SetOnDSR;			// DSR changed state event
		property OnDCD: TNotifyEvent read FOnDCD write SetOnDCD;			// DCD changed state event
		property OnError: TNotifyEvent read FOnError write SetOnError;			// Receive error event
		procedure Open;	override;							// Open serial port
		procedure Close; override;							// Close serial port
		constructor Create(AOwner: TComponent); override;                           	// Create
		destructor Destroy; override;                                               	// Destroy
	end;

	TSerialCom = class(TSerialEvents)
	end;

	EComError = class(Exception);								// Any error in TSerialCom

resourcestring
	ComError = 'Serial communication port error';						// Error message of EComError exception

procedure Register;										// Register component



implementation

type
	TComThread = class(TThread)								// Interrupt handler thread
	private
		Owner: TSerialEvents;								// Serial port component
		Mask: DWORD;									// Happened ComEvents
	protected
		procedure Execute; override;							// Interrupt wait loop
		procedure DoEvents;								// Execute event handlers
	public
		constructor Create(AOwner: TSerialEvents);					// Create
		destructor Destroy; override;							// Destroy
	end;


const
	dcbfBinary           =  0;								// WinAPI control bits
	dcbfParity           =  1;
	dcbfOutxCtsFlow      =  2;
	dcbfOutxDsrFlow      =  3;
	dcbfDtrControl       =  4;
	dcbfDsrSensivity     =  6;
	dcbfTXContinueOnXOff =  7;
	dcbfOutX             =  8;
	dcbfInX              =  9;
	dcbfErrorChar        = 10;
	dcbfNull             = 11;
	dcbfRtsControl       = 12;
	dcbfAbortOnError     = 14;



//----------------------------------------------------------------------------------------------
// Private utilities



// Translate ComEvents to SetCommMask
function ComEventsToMask(ComEvents: TComEvents): DWORD;
begin
	Result:= 0;
	if evRxChar  in ComEvents then Result:= Result or EV_RXCHAR;
	if evTxEmpty in ComEvents then Result:= Result or EV_TXEMPTY;
	if evError   in ComEvents then Result:= Result or EV_ERR;
	if evRing    in ComEvents then Result:= Result or EV_RING;
	if evBreak   in ComEvents then Result:= Result or EV_BREAK;
	if evCTS     in ComEvents then Result:= Result or EV_CTS;
	if evDSR     in ComEvents then Result:= Result or EV_DSR;
	if evDCD     in ComEvents then Result:= Result or EV_RLSD;
end;



//----------------------------------------------------------------------------------------------
// TComThread
// Interrupt handler



// Create and start
constructor TComThread.Create(AOwner: TSerialEvents);
begin
	inherited Create(True);         							// Create suspended
	Owner:= AOwner;
	Priority:= tpTimeCritical;                                                 		// Highest priority
end;



// Get and execute interrupt
procedure TComThread.Execute;
var
	EventHandles: Array[0..1] of THandle;
	Overlapped: TOverlapped;
	BytesTrans: DWORD;
begin
	FillChar(Overlapped, SizeOf(Overlapped), 0);
	Overlapped.hEvent:= CreateEvent(nil, True, True, nil);
	EventHandles[0]:= Owner.CloseEvent;
	EventHandles[1]:= Overlapped.hEvent;
	repeat
		WaitCommEvent(Owner.Handle, Mask, @Overlapped);
		WaitForMultipleObjects(2, @EventHandles, False, INFINITE);
		if GetOverlappedResult(Owner.Handle, Overlapped, BytesTrans, False) then
			DoEvents;
	until Terminated;									// Terminate if requested
	CloseHandle(Overlapped.hEvent);
end;



// Reset interrupt
destructor TComThread.Destroy;
begin
	inherited Destroy;               							// Wait for terminate and destroy
end;



// Interrupt routines
procedure TComThread.DoEvents;
begin

	// Clear errors automatically
	if ((EV_ERR and Mask)<>0) or ((EV_BREAK and Mask)<>0) then
	begin		 									// Clear errors
		Owner.ComErrors;
	end;

	// Execute event handlers
	if ((EV_ERR and Mask)<>0)     and Assigned(Owner.FOnError)   then Owner.FOnError(Owner);
	if ((EV_BREAK and Mask)<>0)   and Assigned(Owner.FOnRxBreak) then Owner.FOnRxBreak(Owner);
	if ((EV_RXCHAR and Mask)<>0)  and Assigned(Owner.FOnRxChar)  then Owner.FOnRxChar(Owner);
	if ((EV_TXEMPTY and Mask)<>0) and Assigned(Owner.FOnTxEmpty) then Owner.FOnTxEmpty(Owner);
	if ((EV_RING and Mask)<>0)    and Assigned(Owner.FOnRI)      then Owner.FOnRI(Owner);
	if ((EV_CTS and Mask)<>0)     and Assigned(Owner.FOnCTS)     then Owner.FOnCTS(Owner);
	if ((EV_DSR and Mask)<>0)     and Assigned(Owner.FOnDSR)     then Owner.FOnDSR(Owner);
	if ((EV_RLSD and Mask)<>0)    and Assigned(Owner.FOnDCD)     then Owner.FOnDCD(Owner);
end;



//----------------------------------------------------------------------------------------------
// TSerialFile
// Minimal functions: open, close, read, write



// Create and initialize
constructor TSerialFile.Create(AOwner: TComponent);
begin
	inherited Create(AOwner);
	FConnected:= False;
	FBaudRate:= 9600;
	FParity:= prNone;
	FPortNumber:= 1;
	FStopBits:= sbOneStopBit;
	FDataBits:= 8;
	FReadTimeout:= 0;									// Return immediately
	FWriteTimeout:= 0;									// Return immediately
	FHandle:= INVALID_HANDLE_VALUE;
	CloseEvent:= CreateEvent(nil, True, False, nil);					// Signal close
end;



// Close and destroy
destructor TSerialFile.Destroy;
begin
	Close;                                                                       		// Close file
	CloseHandle(CloseEvent);								// Close signal
	inherited Destroy;
end;



// Serial port system name
function TSerialFile.ComString: String;
begin
	Result:= '\\.\COM'+IntToStr(FPortNumber);						// Good for >COM9 too
end;



// Check handle
function TSerialFile.ValidHandle: Boolean;
begin
	Result:= Handle <> INVALID_HANDLE_VALUE;
end;



// Open file for serial port
procedure TSerialFile.CreateHandle;
begin
	FHandle:= CreateFile(
		PChar(ComString),
		GENERIC_READ or GENERIC_WRITE,
		0,
		nil,
		OPEN_EXISTING,
		FILE_FLAG_OVERLAPPED,
		0);
	if not ValidHandle then
		raise EComError.Create(ComError);
end;



// Close file for serial port
procedure TSerialFile.DestroyHandle;
begin
	if ValidHandle then CloseHandle(Handle);
	FHandle:= INVALID_HANDLE_VALUE;
end;



// Check the existence of a port
function TSerialFile.Exist: Boolean;
begin
	if FConnected then
		Result:= True
	else
	begin
		FHandle:= CreateFile(
			PChar(ComString),
			0,									// Query
			0,
			nil,
			OPEN_EXISTING,
			FILE_FLAG_OVERLAPPED,
			0);
		Result:= ValidHandle;
		DestroyHandle;
	end;
end;



// Open serial port
procedure TSerialFile.Open;
begin
	Close;                                   						// Reopen?
	CreateHandle;                            						// Create file handle
	ResetEvent(CloseEvent);									// Enable I/O
	Setup;                                   						// Set parameters
	FConnected:= True;                       						// Port is open
end;



// Close serial port
procedure TSerialFile.Close;
begin
	if FConnected then
	begin											// If open
		FConnected:= False;                         					// Port is closed
		SetCommMask(Handle, 0);								// No interrupts
		PurgeComm(Handle, PURGE_TXABORT or PURGE_RXABORT or
				  PURGE_TXCLEAR or PURGE_RXCLEAR);
		SetEvent(CloseEvent);								// Signal I/O end
	end;
	DestroyHandle;										// Close file
end;



// Setup serial port parameters
procedure TSerialFile.Setup;
const
	RXBUFSIZE=4096;										// Receive buffer size
	TXBUFSIZE=2048;										// Transmit buffer size
var
	DCB: TDCB;
	Timeouts: TCommTimeouts;
begin

	// Main parameters
	GetCommState(Handle, DCB);
	DCB.BaudRate:= FBaudRate;
	DCB.Flags:= (1 shl dcbfBinary) or (1 shl dcbfParity) or (1 shl dcbfTXContinueOnXoff);
	DCB.Flags:= DCB.Flags or (DTR_CONTROL_ENABLE shl dcbfDtrControl);			// DTR=1, no handshake
	DCB.Flags:= DCB.Flags or (RTS_CONTROL_ENABLE shl dcbfRtsControl);			// RTS=1, no handshake
	DCB.ByteSize:= FDataBits;
	case FParity of
		prNone:  DCB.Parity:= NOPARITY;
		prOdd:   DCB.Parity:= ODDPARITY;
		prEven:  DCB.Parity:= EVENPARITY;
		prMark:  DCB.Parity:= MARKPARITY;
		prSpace: DCB.Parity:= SPACEPARITY;
	end;
	case FStopBits of
		sbOneStopBit:      DCB.StopBits:= ONESTOPBIT;
		sbOneHalfStopBits: DCB.StopBits:= ONE5STOPBITS;
		sbTwoStopBits:     DCB.StopBits:= TWOSTOPBITS;
	end;
	DCB.XonChar:= #17;
	DCB.XoffChar:= #19;
	DCB.XonLim:= RXBUFSIZE div 4;
	DCB.XoffLim:= RXBUFSIZE div 4;
	if not SetCommState(Handle, DCB) then
		raise EComError.Create(ComError);

	// Timeouts
	Timeouts.ReadIntervalTimeout:= MAXDWORD;
	Timeouts.ReadTotalTimeoutMultiplier:= 0;
	Timeouts.ReadTotalTimeoutConstant:= FReadTimeout;
	Timeouts.WriteTotalTimeoutMultiplier:= 0;
	Timeouts.WriteTotalTimeoutConstant:= FWriteTimeout;
	SetCommTimeouts(Handle, Timeouts);

	// Buffer size
	if not SetupComm(Handle, RXBUFSIZE, TXBUFSIZE) then
		raise EComError.Create(ComError);

	// No interrupts
	SetCommMask(Handle, 0);

end;



// Read error flags
function TSerialFile.GetComErrors: TComErrors;
var
	Errors: DWORD;
begin
	Result:= [];
	if FConnected then
	begin
		ClearCommError(Handle, Errors, nil);
		if 0<>(Errors and CE_FRAME)     then Result:= Result + [ceFrame];
		if 0<>(Errors and CE_RXPARITY)  then Result:= Result + [ceParity];
		if 0<>(Errors and CE_OVERRUN)   then Result:= Result + [ceOverrun];
		if 0<>(Errors and CE_BREAK)     then Result:= Result + [ceBreak];
	end;
end;



// Write timeout
procedure TSerialFile.SetWriteTimeout(Value: Cardinal);
var
	Timeouts: TCommTimeouts;
begin
	FWriteTimeout:= Value;
	if Connected then
	begin
		GetCommTimeouts(Handle, Timeouts);
		Timeouts.WriteTotalTimeoutConstant:= FWriteTimeout;
		SetCommTimeouts(Handle, Timeouts);
	end;
end;



// Read timeout
procedure TSerialFile.SetReadTimeout(Value: Cardinal);
var
	Timeouts: TCommTimeouts;
begin
	FReadTimeout:= Value;
	if Connected then
	begin
		GetCommTimeouts(Handle, Timeouts);
		Timeouts.ReadTotalTimeoutConstant:= FReadTimeout;
		SetCommTimeouts(Handle, Timeouts);
	end;
end;



// Write data
// Return if all data has been written, timeout elapsed or port is closed
// Return the number of bytes written
function TSerialFile.Write(var Buffer; Count: Integer): Integer;
var
	EventHandles: Array[0..1] of THandle;
	Overlapped: TOverlapped;
	BytesWritten: DWORD;
begin
	Result:= 0;
	if FConnected then
	begin
		FillChar(Overlapped, SizeOf(Overlapped), 0);
		Overlapped.hEvent:= CreateEvent(nil, True, True, nil);
		EventHandles[0]:= CloseEvent;
		EventHandles[1]:= Overlapped.hEvent;
		WriteFile(Handle, Buffer, Count, BytesWritten, @Overlapped);
		WaitForMultipleObjects(2, @EventHandles, False, INFINITE);
		if GetOverlappedResult(Handle, Overlapped, BytesWritten, False) then
			Result:= BytesWritten;
		CloseHandle(Overlapped.hEvent);
	end;
end;



// Write data
// Return if all data has been written, timeout elapsed or port is closed
// Return the number of bytes written
function TSerialFile.WriteString(Str: String): Integer;
begin
	Result:= Write(Str[1], Length(Str));
end;



// Write byte
// Return if all data has been written, timeout elapsed or port is closed
// Return the byte or -1 on error
function TSerialFile.Put(b: Byte): Integer;
begin
	if Write(b, 1)>0 then
		Result:= b
	else
		Result:= -1;
end;



// Read data
// Return if all data has been read, timeout elapsed or port is closed
// Return the number of bytes read
function TSerialFile.Read(var Buffer; Count: Integer): Integer;
var
	EventHandles: Array[0..1] of THandle;
	Overlapped: TOverlapped;
	BytesRead: DWORD;
begin
	Result:= 0;
	if FConnected then
	begin
		FillChar(Overlapped, SizeOf(Overlapped), 0);
		Overlapped.hEvent:= CreateEvent(nil, True, True, nil);
		EventHandles[0]:= CloseEvent;
		EventHandles[1]:= Overlapped.hEvent;
		ReadFile(Handle, Buffer, Count, BytesRead, @Overlapped);
		WaitForMultipleObjects(2, @EventHandles, False, INFINITE);
		if GetOverlappedResult(Handle, Overlapped, BytesRead, False) then
			Result:= BytesRead;
		CloseHandle(Overlapped.hEvent);
	end;
end;



// Read data
// Return if all data has been read, timeout elapsed or port is closed
// Return the number of bytes read
function TSerialFile.ReadString(var Str: String; Count: Integer): Integer;
begin
	SetLength(Str, Count);
		Result:= Read(Str[1], Count);
	SetLength(Str, Result);
end;



// Read byte
// Return if all data has been read, timeout elapsed or port is closed
// Return the byte or -1 on error
function TSerialFile.Get: Integer;
var
	b: Byte;
begin
	b:= 0;
	if Read(b, 1)>0 then
		Result:= b
	else
		Result:= -1;
end;



//----------------------------------------------------------------------------------------------
// TSerialControl
// Control signals



// Create and initialize
constructor TSerialControls.Create(AOwner: TComponent);
begin
	inherited Create(AOwner);
	FDTRControl:= dtrNone;
	FDTRState:= True;
	FRTSControl:= rtsNone;
	FRTSState:= True;
	FXONXOFFControl:= False;
end;



// Close and destroy
destructor TSerialControls.Destroy;
begin
	inherited Destroy;
end;



// Open serial port
procedure TSerialControls.Open;
begin
	inherited Open;
	if FTxBreak then TxBreak:= FTxBreak;     						// Execute pending break
end;



// Setup serial port parameters
procedure TSerialControls.Setup;
var
	DCB: TDCB;
begin

	inherited Setup;

	// Get DCB
	GetCommState(Handle, DCB);

	// Modify controls in DCB
	DCB.Flags:= DCB.Flags and not(
		(1 shl dcbfOutxDsrFlow) or
		(1 shl dcbfOutxCtsFlow) or
		(3 shl dcbfDtrControl) or
		(1 shl dcbfOutX) or (1 shl dcbfInX) or
		(3 shl dcbfRtsControl));

	if (FDTRControl=dtrFlow) then DCB.Flags:= DCB.Flags or (1 shl dcbfOutxDsrFlow);
	if (FRTSControl=rtsFlow) then DCB.Flags:= DCB.Flags or (1 shl dcbfOutxCtsFlow);
	case FDTRControl of
		dtrNone: if FDTRState then
				DCB.Flags:= DCB.Flags or (DTR_CONTROL_ENABLE shl dcbfDtrControl)
			else
				DCB.Flags:= DCB.Flags or (DTR_CONTROL_DISABLE shl dcbfDtrControl);
		dtrFlow:	DCB.Flags:= DCB.Flags or (DTR_CONTROL_HANDSHAKE shl dcbfDtrControl);
	end;
	if FXONXOFFControl then DCB.Flags:= DCB.Flags or (1 shl dcbfOutX) or (1 shl dcbfInX);
	case FRTSControl of
		rtsNone: if FRTSState then
				DCB.Flags:= DCB.Flags or (RTS_CONTROL_ENABLE shl dcbfRtsControl)
			 else
				DCB.Flags:= DCB.Flags or (RTS_CONTROL_DISABLE shl dcbfRtsControl);
		rtsFlow:	DCB.Flags:= DCB.Flags or (RTS_CONTROL_HANDSHAKE shl dcbfRtsControl);
		rtsDirection:	DCB.Flags:= DCB.Flags or (RTS_CONTROL_TOGGLE shl dcbfRtsControl);
	end;

	// Set DCB
	if not SetCommState(Handle, DCB) then
		raise EComError.Create(ComError);

end;



// Set DTR if not automatic
procedure TSerialControls.SetDTRState(Value: Boolean);
var
	Control: DWORD;
begin
	if FConnected then
	begin
		if FDTRControl=dtrNone then
		begin
			FDTRState:= Value;
			if Value then Control:= SETDTR else Control:= CLRDTR;
			if not EscapeCommFunction(FHandle, Control) then
				raise EComError.Create(ComError);
		end;
	end
	else FDTRState:= Value;
end;



// Set RTS if not automatic
procedure TSerialControls.SetRTSState(Value: Boolean);
var
	Control: DWORD;
begin
	if FConnected then
	begin
		if FRTSControl=rtsNone then
		begin
			FRTSState:= Value;
			if Value then Control:= SETRTS else Control:= CLRRTS;
			if not EscapeCommFunction(FHandle, Control) then
				raise EComError.Create(ComError);
		end;
	end
	else FRTSState:= Value;
end;



// Get CTS
function TSerialControls.GetCTSState: Boolean;
var
	Status: DWORD;
begin
	GetCommModemStatus(FHandle, Status);
	Result:= ((Status and MS_CTS_ON) <> 0);
end;



// Get DSR
function TSerialControls.GetDSRState: Boolean;
var
	Status: DWORD;
begin
	GetCommModemStatus(FHandle, Status);
	Result:= ((Status and MS_DSR_ON) <> 0);
end;



// Get DCD
function TSerialControls.GetDCDState: Boolean;
var
	Status: DWORD;
begin
	GetCommModemStatus(FHandle, Status);
	Result:= ((Status and MS_RLSD_ON) <> 0);
end;



// Get RI
function TSerialControls.GetRIState: Boolean;
var
	Status: DWORD;
begin
	GetCommModemStatus(FHandle, Status);
	Result:= ((Status and MS_RING_ON) <> 0);
end;



// Set or clear break
procedure TSerialControls.SetTxBreak(Value: Boolean);
var
	Control: DWORD;
begin
	if FConnected then
	begin
		FTxBreak:= Value;
		if Value then Control:= SETBREAK else Control:= CLRBREAK;
		EscapeCommFunction(FHandle, Control);
	end
	else FTxBreak:= Value;
end;



//----------------------------------------------------------------------------------------------
// TSerialWaits
// Wait for serial port events



// Wait for serial port events
procedure TSerialWaits.WaitFor(ComEvents: TComEvents; Timeout: Cardinal);
var
	EventHandles: array [0..1] of THandle;
	Overlapped: TOverlapped;
	AMask: DWORD;
begin
	FillChar(Overlapped, SizeOf(Overlapped), 0);
	Overlapped.hEvent:= CreateEvent(nil, True, True, nil);
	EventHandles[0]:= CloseEvent;
	EventHandles[1]:= Overlapped.hEvent;
	AMask:= ComEventsToMask(ComEvents);
	SetCommMask(Handle, AMask);
	WaitCommEvent(Handle, AMask, @Overlapped);
	WaitForMultipleObjects(2, @EventHandles, False, Timeout);
	SetCommMask(Handle, 0);
	CloseHandle(Overlapped.hEvent);
end;



//----------------------------------------------------------------------------------------------
// TSerialEvents
// Execute functions on serial port events



// Create and initialize
constructor TSerialEvents.Create(AOwner: TComponent);
begin
	inherited Create(AOwner);
	FComEvents:= [];										// No ComEvents
	EventThread:= TComThread.Create(Self);   						// Create interrupt handler
end;



// Close and destroy
destructor TSerialEvents.Destroy;
begin
	EventThread.Free;	                           					// Destroy interrupt handler
	inherited Destroy;
end;



// Open serial port
procedure TSerialEvents.Open;
begin
	inherited Open;
	EventThread.Resume;									// Start interrupt handler
	if Assigned(FOnOpen) then FOnOpen(Self);						// Execute OnOpen event handler
end;



// Close serial port
procedure TSerialEvents.Close;
begin
	if FConnected then
	begin											// If open
		if Assigned(FOnClose) then FOnClose(Self);					// Execute OnClose event handler
		EventThread.Suspend;								// Request close of eventthread
		SetCommMask(Handle, 0);								// No interrupts
		SetEvent(CloseEvent);								// Signal I/O to end
	end;
	inherited Close;
end;



// Setup serial port parameters
procedure TSerialEvents.Setup;
begin
	inherited Setup;

	// Set interrupt mask
	SetComEvents(ComEvents);
end;



// Set ComEvents mask and enable corresponding interrupts
procedure TSerialEvents.SetComEvents(Value: TComEvents);
begin
	FComEvents:= Value;
	if ValidHandle then									// If port is open
		SetCommMask(Handle, ComEventsToMask(Value));					// Translate and set interrupt mask
end;



// Do something on received character
procedure TSerialEvents.SetOnRxChar(Value: TNotifyEvent);
begin
	if Assigned(Value) then
		ComEvents:= ComEvents-[evRxChar]
	else
		ComEvents:= ComEvents+[evRxChar];
	FOnRxChar:= Value;
end;



// Do something on transmit FIFO empty
procedure TSerialEvents.SetOnTxEmpty(Value: TNotifyEvent);
begin
	if Assigned(Value) then
		ComEvents:= ComEvents-[evTxEmpty]
	else
		ComEvents:= ComEvents+[evTxEmpty];
	FOnTxEmpty:= Value;
end;



// Do something on received break
procedure TSerialEvents.SetOnRxBreak(Value: TNotifyEvent);
begin
	if Assigned(Value) then
		ComEvents:= ComEvents-[evBreak]
	else
		ComEvents:= ComEvents+[evBreak];
	FOnRxBreak:= Value;
end;



// Do something on RI rising edge
procedure TSerialEvents.SetOnRI(Value: TNotifyEvent);						// Set property OnRI
begin
	if Assigned(Value) then
		ComEvents:= ComEvents-[evRing]
	else
		ComEvents:= ComEvents+[evRing];
	FOnRI:= Value;
end;



// Do something on CTS change
procedure TSerialEvents.SetOnCTS(Value: TNotifyEvent);					// Set property OnCTS
begin
	if Assigned(Value) then
		ComEvents:= ComEvents-[evCTS]
	else
		ComEvents:= ComEvents+[evCTS];
	FOnCTS:= Value;
end;



// Do something on DSR change
procedure TSerialEvents.SetOnDSR(Value: TNotifyEvent);					// Set property OnDSR
begin
	if Assigned(Value) then
		ComEvents:= ComEvents-[evDSR]
	else
		ComEvents:= ComEvents+[evDSR];
	FOnDSR:= Value;
end;



// Do something on DCD change
procedure TSerialEvents.SetOnDCD(Value: TNotifyEvent);
begin
	if Assigned(Value) then
		ComEvents:= ComEvents-[evDCD]
	else
		ComEvents:= ComEvents+[evDCD];
	FOnDCD:= Value;
end;



// Do something on error
procedure TSerialEvents.SetOnError(Value: TNotifyEvent);
begin
	if Assigned(Value) then
		ComEvents:= ComEvents-[evError]
	else
		ComEvents:= ComEvents+[evError];
	FOnError:= Value;
end;



//----------------------------------------------------------------------------------------------
// Component specific



// Register component
procedure Register;
begin
	RegisterComponents('Propix', [TSerialCom, TSerialEvents, TSerialControls, TSerialFile]);
end;



end.
