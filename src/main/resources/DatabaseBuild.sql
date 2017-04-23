CREATE TABLE IF NOT EXISTS FILES(
    FileID INTEGER PRIMARY KEY,
    FileName varchar(50) not null,
    FilePath varchar(500) not null,
    LastModDate DATE not null,
    DateAdded DATE DEFAULT CURRENT_TIMESTAMP,
    Hits int DEFAULT 0,
    Hash CHAR(32),
    UNIQUE (FilePath)
);

CREATE TABLE IF NOT EXISTS TAGS(
    TagID INTEGER PRIMARY KEY,
    TagText varchar(50) not null,
    UNIQUE (TagText)
);

CREATE TABLE IF NOT EXISTS FILE_TAGS(
    FileID int not null,
    TagID int not null,
    FOREIGN KEY (FileID) REFERENCES FILES(FileID),
    FOREIGN KEY (TagID) REFERENCES TAGS(rowid),
    PRIMARY KEY (FileID,TagID)
);

CREATE TABLE IF NOT EXISTS GROUPS(
    GroupID INTEGER PRIMARY KEY,
    GroupName varchar(100) not null,
    UNIQUE (GroupName)
);

CREATE TABLE IF NOT EXISTS GROUP_FILES(
    GroupID int not null,
    FileID int not null,
    FOREIGN KEY (GroupID) REFERENCES GROUPS(GroupID),
    FOREIGN KEY (FileID) REFERENCES FILES(FileID)
);

CREATE VIEW IF NOT EXISTS TAG_ASSOCIATIONS AS
SELECT ft1.TagID AS tag1, ft2.TagID AS tag2, COUNT(ft1.FileID) AS count
FROM FILE_TAGS ft1, FILE_TAGS ft2
WHERE ft1.FileID=ft2.FileID AND ft1.rowid <> ft2.rowid
GROUP BY ft1.TagID, ft2.TagID;
