
% Create TCP/IP object 't'. Specify server machine and port number. 
t = tcpip('localhost', 1234);    

% Set size of receiving buffer, if needed. 
set(t, 'InputBufferSize', 30000);

% Open connection to the server. 
fopen(t);       


% Set the beginning hour and storage capacity at the beginning
stunde=2400;%2400 6590
Ecth=6;

% Create the matric to store the received data 
RecordChp1=0;
RecordChp2=0;
RecordBoiler=0;
RecordStorage=0;
RecordCom=0;
RecordDev=0;
RecordTime=0;
RecordEth=0;
RecordPrequired=0;

% Set the loop
for i=1:49
  collect=0;
  
% Set the sampling time
pause(4)
while (get(t, 'BytesAvailable') > 0)
t.BytesAvailable
DataReceived = fscanf(t) ;
DataReceived=str2double(DataReceived);
collect=[collect;DataReceived];
end

% Get the commands
chp1=collect(2,1);
chp2=collect(3,1);
boiler=collect(4,1);
Pre=collect(5,1);
Pch=collect(6,1);

% Run the simulation and get Pconsumption and Eth 
sim('Heating');

% Record the all necessary values
RecordChp1=[RecordChp1, Pchp1];
RecordChp2=[RecordChp2, Pchp2];
RecordBoiler=[RecordBoiler, Pboiler];
RecordStorage=[RecordStorage, Pstorage];
RecordCom=[RecordCom, Pcom];
RecordDev=[RecordDev, Pdev];
RecordTime=[RecordTime, stunde];
RecordEth=[RecordEth, Eth];
RecordPrequired=[RecordPrequired, Prequired];

% Change to next hour
% Refresh the current capacity
stunde=stunde+1;
Ecth=Eth;

%send the result Pconsumption and Eth to TCP agent
Pcom=num2str(Pcom);
Eth=num2str(Eth);
fprintf(t, Pcom);
fprintf(t, Eth);
fprintf(t, '');


end
% End
fclose(t); 
delete(t); 
clear t 








