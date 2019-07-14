const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

exports.useWildcard = functions.firestore
    .document('UserWithUidAndPoints/{userId}')
    .onWrite((change, context) => {
        const data = change.after.data();
      const previousData = change.before.data();

      // We'll only update if changed.
      // This is crucial to prevent infinite loops.
      if (data.Invited > 0 && previousData.Invited < 0 ){
        let count = data.Points;
        let refId = data.InvitedBy;
        
        var points = admin.firestore().collection("PointsValue").doc("ADMIN-12345678");

        return points
        .get().then(doc =>{
          console.log('XXXXXXX',doc.data());
            admin.firestore().collection("UserWithUidAndPoints").doc(refId).update({
              "TotalRefers": admin.firestore.FieldValue.increment(1),
              "Points": admin.firestore.FieldValue.increment(doc.data().InviteeX)
            })

            return change.after.ref.set({
              Points: count + 25
            }, {merge: true});

        }).catch(err =>{
            console.log('Error',err);
            return false;
        });

      }else{
        return null;  
      }
    });

    
    
    