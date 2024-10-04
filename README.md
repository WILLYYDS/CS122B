# CS 122B Project 1 For CDC Disney
### Authors: 
#### - Jiayun Wang, JiaLiang Huang

### Contributions for each member:
#### - Jiayun Wang 50%    #16742752 
#### - Jialiang Huang 50%    #41101462

#### Project 2
#### Jiayun Wang - Servlet + Js + HTML + Github + MySQL + Debug + Demo
### Jialiang Huang - HTML + Project setup +  + CSS + AWS + Session/Session Storage

LIKE/ILIKE：

In the getInputQuery method, the WHERE clause of the SQL query is constructed based on the input query conditions. When the query condition is title, director, or star name, the LIKE predicate is used for fuzzy matching.
if (inputArr[i].equals("title")) {
    inputQuery += "m.title LIKE '%" + inputArr[i + 1] + "%' COLLATE utf8mb4_general_ci ";
} else if (inputArr[i].equals("director")) {
    inputQuery += "m.director LIKE '%" + inputArr[i + 1] + "%' COLLATE utf8mb4_general_ci ";
} else if (inputArr[i].equals("name")) {
    inputQuery += "s.name LIKE '%" + inputArr[i + 1] + "%' COLLATE utf8mb4_general_ci ";
}
When handling the alphabetical query condition, the LIKE predicate is also used for prefix matching.
if (inputArr[1].equals("*")) {
    inputQuery += "m.title REGEXP '^[^a-zA-Z0-9]'";
} else {
    inputQuery += "m.title LIKE '" + inputArr[1] + "%' OR m.title LIKE '" + inputArr[1].toLowerCase() + "%' ";
}

# Demo Video

### - Project 1 Demo Video here：https://drive.google.com/file/d/1K5AAdRiwdrTMpG3zFNxRlDfILC-0Jx-Q/view?usp=sharing

### — Project 2 Demo Video here: https://drive.google.com/drive/folders/1q9WkuCPKhS-dBRyc8rxXMJ19d77zmLpy?usp=drive_link

# Instructions Login to the AWS

Instance ID: i-00f891bc393235c23 (Will's Service)

1. Obtain SSH access.
2. Ensure the key file is secured. After downloading the key file named KeyForWill.pem,
3. Set proper permissions to ensure the key file is not overly permissive.
```
chmod 400 "KeyForWill.pem"
```
4. Connect using the DNS provided for the instance:
```
ec2-34-216-228-170.us-west-2.compute.amazonaws.com
```

Example Command
```
ssh -i "KeyForWill.pem" ubuntu@ec2-34-216-228-170.us-west-2.compute.amazonaws.com
```


# Mysql database 
### `moviedbe`

```
local> mysql -u mytestuser -p 
(Password: My6$Password)
mysql> SHOW DATABASES;
mysql> USE moviedbe;
```

# Deploy Locally on Development Machine

1. IntelliJ Configuration
2. Import Project from External Model > Choose Maven

