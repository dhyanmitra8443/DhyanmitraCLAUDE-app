-- openapi.yaml's LessonSummary schema documents a shortDescription field
-- (Ref: SRS Chapter 7) that sections already have (sections.short_description)
-- but lessons never got when V1 was written. Additive, nullable, no backfill.
ALTER TABLE lessons ADD COLUMN short_description TEXT;
