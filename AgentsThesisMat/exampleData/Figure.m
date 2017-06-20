%Set the record time
RecordTime=[1:1:48];

figure;
title('Diagrams')
plot(RecordTime, RecordChp1, 'blue', RecordTime, RecordChp2,'red', RecordTime, RecordBoiler,'green', RecordTime, RecordStorage, 'yellow', RecordTime, RecordCom, 'blue', RecordTime, RecordDev, 'black')
legend('CHP1','CHP2','Boiler','Storage', 'Consumption', 'Dev')
legend('Location', 'NorthWest')

figure(2);
% subplot (nrows,ncols,plot_nuber)
x=RecordTime; 
subplot(3,2,1); 
plot(x,RecordChp1);
title('CHP1');

subplot(3,2,2); 
plot(x,RecordChp2);
title('CHP2');

subplot(3,2,3);
plot(x,RecordBoiler);
title('Boiler');

subplot(3,2,4) 
plot(x,RecordStorage);
title('Storage');

subplot(3,2,5) 
plot(x,RecordCom);
title('Consumption');

subplot(3,2,6); 
plot(x, RecordDev);
title('Deviation');


