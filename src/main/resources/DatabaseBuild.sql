CREATE TABLE IF NOT EXISTS FILES(
    FileName varchar(50),
    FileDirectory varchar(100),
    FileID int,
    Hits int,
    PRIMARY KEY (FileID)
);

CREATE TABLE IF NOT EXISTS TAGS(
    TagID int not null,
    TagName varchar(50),
    PRIMARY KEY(TagID)
);

CREATE TABLE IF NOT EXISTS TAG_RELATIONS(
    TagID1 int not null,
    TagID2 int not null,
    FOREIGN KEY (TagID1) REFERENCES TAGS(TagID),
    FOREIGN KEY (TagID1) REFERENCES TAGS(TagID),
    PRIMARY KEY (TagID1,TagID2)
);

CREATE TABLE IF NOT EXISTS FILE_TAGS(
    FileID int not null,
    TagID int not null,
    FOREIGN KEY (FileID) REFERENCES FILES(FileID),
    FOREIGN KEY (TagID) REFERENCES TAGS(TagID),
    PRIMARY KEY (FileID,TagID)
);